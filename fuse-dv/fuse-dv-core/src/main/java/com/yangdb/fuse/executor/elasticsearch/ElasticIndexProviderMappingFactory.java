package com.yangdb.fuse.executor.elasticsearch;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Inject;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.Entity;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.model.schema.Relation;
import javaslang.Tuple2;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Collections.singletonMap;

/**
 * generate elastic mapping template according to ontology and index provider schema
 * generate the indices according to the index provider partitions
 */
public class ElasticIndexProviderMappingFactory {

    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String DIRECTION = "direction";
    public static final String ENTITY_A = "entityA";
    public static final String ENTITY_B = "entityB";
    public static final String PROPERTIES = "properties";
    public static final String NESTED = "nested";
    public static final String CHILD = "child";
    public static final String EMBEDDED = "embedded";

    private Client client;
    private RawSchema schema;
    private IndexProvider indexProvider;
    private Ontology.Accessor ontology;

    @Inject
    public ElasticIndexProviderMappingFactory(Client client, RawSchema schema, Ontology ontology, IndexProvider indexProvider) {
        this.client = client;
        this.schema = schema;
        this.indexProvider = indexProvider;
        this.ontology = new Ontology.Accessor(ontology);
    }

    /**
     * create indices according to ontology
     *
     * @return
     */
    public List<String> createIndices() {
        List<CreateIndexResponse> responses = new ArrayList<>();
        StreamSupport.stream(schema.indices().spliterator(), false).forEach(name -> {
            CreateIndexRequest request = new CreateIndexRequest(name);
            responses.add(client.admin().indices().create(request).actionGet());
        });
        return responses.stream().map(CreateIndexResponse::index).collect(Collectors.toList());
    }

    public List<Tuple2<String, Boolean>> generateMappings() {
        List<Tuple2<String, AcknowledgedResponse>> responses = new ArrayList<>();
        responses.addAll(mapEntities(client).stream()
                .map(r -> new Tuple2<>(r.request().name(), r.execute().actionGet()))
                .collect(Collectors.toList()));
        responses.addAll(mapRelations(client).stream()
                .map(r -> new Tuple2<>(r.request().name(), r.execute().actionGet()))
                .collect(Collectors.toList()));
        return responses.stream().map(r -> new Tuple2<>(r._1, r._2.isAcknowledged()))
                .collect(Collectors.toList());
    }

    /**
     * add the mapping part of the template according to the ontology relations
     *
     * @return
     */
    private List<ESPutIndexTemplateRequestBuilder> mapRelations(Client client) {
        List<ESPutIndexTemplateRequestBuilder> requests = new ArrayList<>();
        ontology.relations().forEach(r -> {
            String mapping = indexProvider.getRelation(r.getName()).orElseThrow(
                    () -> new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No entity with name " + r + " found in ontology")))
                    .getPartition();

            Relation relation = indexProvider.getRelation(r.getName()).get();
            MappingIndexType type = MappingIndexType.valueOf(mapping.toUpperCase());
            switch (type) {
                case UNIFIED:
                case STATIC:
                    //static index
                    relation.getProps().getValues().forEach(v -> {
                        String label = r.getrType();
                        ESPutIndexTemplateRequestBuilder request =  new ESPutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE,label.toLowerCase());
                        request.setPatterns(Arrays.asList(r.getName().toLowerCase(), label, r.getName(), String.format("%s%s", v, "*")))
                                .setSettings(generateSettings(r, relation, label))
                                .addMapping(label, generateMapping(r, relation, label));
                        //add response to list of responses
                        requests.add(request);
                    });
                    break;
                case TIME:
                    String label = r.getrType();
                    ESPutIndexTemplateRequestBuilder request = new ESPutIndexTemplateRequestBuilder(client,PutIndexTemplateAction.INSTANCE,relation.getType().toLowerCase());
                    request.setPatterns(Arrays.asList(r.getName().toLowerCase(), label, r.getName(), String.format(relation.getProps().getIndexFormat(), "*")))
                            .setSettings(generateSettings(r, relation, label))
                            .addMapping(label, generateMapping(r, relation, label));
                    //add response to list of responses
                    requests.add(request);
                    break;
                default:
                    String result = "No mapping found";
                    break;
            }
        });
        return requests;
    }

    /**
     * add the mapping part of the template according to the ontology entities
     *
     * @return
     * @param client
     */
    private List<ESPutIndexTemplateRequestBuilder> mapEntities(Client client) {
        List<ESPutIndexTemplateRequestBuilder> requests = new ArrayList<>();
        StreamSupport.stream(ontology.entities().spliterator(), false)
                .forEach(e -> {
            String mapping = indexProvider.getEntity(e.getName()).orElseThrow(
                    () -> new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No entity with name " + e + " found in ontology")))
                    .getPartition();

            Entity entity = indexProvider.getEntity(e.getName()).get();
            try {
                MappingIndexType type = MappingIndexType.valueOf(mapping.toUpperCase());
                switch (type) {
                    case UNIFIED:
                        //common general index - unifies all entities under the same physical index
                        entity.getProps().getValues().forEach(v -> {
                            String label = e.geteType();
                            String unifiedName = entity.getProps().getValues().isEmpty() ?  label : entity.getProps().getValues().get(0);
                            Optional<ESPutIndexTemplateRequestBuilder> indexTemplateRequest = requests.stream().filter(p -> p.request().name().equals(unifiedName)).findFirst();
                            ESPutIndexTemplateRequestBuilder request = indexTemplateRequest.orElseGet(() -> new ESPutIndexTemplateRequestBuilder(client,PutIndexTemplateAction.INSTANCE,unifiedName));

                            List<String> patterns = Arrays.asList(e.getName().toLowerCase(), label, e.getName(), String.format("%s%s", v, "*"));
                            if (Objects.isNull(request.request().patterns())) {
                                request.setPatterns(new ArrayList<>(patterns));
                            } else {
                                request.request().patterns().addAll(patterns);
                            }
                            //no specific index sort order since it contains multiple entity types -
                            if (request.request().settings().isEmpty()) {
                                request.setSettings(getSettings());
                            }
                            //create new mapping only when no prior entity set this mapping before
                            if (request.request().mappings().isEmpty()) {
                                request.addMapping(unifiedName, generateMapping(e, entity, unifiedName));
                            } else {
                                populateProperty(entity,request.getMappingsProperties(unifiedName),e);
                            }
                            //add response to list of responses
                            requests.add(request);
                        });

                        break;
                    case STATIC:
                        //static index
                        entity.getProps().getValues().forEach(v -> {
                            String label = e.geteType();
                            ESPutIndexTemplateRequestBuilder request = new ESPutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE,v.toLowerCase());
                            request.setPatterns(Arrays.asList(e.getName().toLowerCase(), label, e.getName(), String.format("%s%s", v, "*")))
                                    .setSettings(generateSettings(e, entity, label))
                                    .addMapping(label, generateMapping(e, entity, label));
                            //add response to list of responses
                            requests.add(request);
                        });
                        break;
                    case TIME:
                        //time partitioned index
                        ESPutIndexTemplateRequestBuilder request = new ESPutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE,e.getName().toLowerCase());
                        String label = entity.getType();
                        request.setPatterns(Arrays.asList(e.getName().toLowerCase(), label, e.getName(), String.format(entity.getProps().getIndexFormat(), "*")))
                                .setSettings(generateSettings(e, entity, label))
                                .addMapping(label, generateMapping(e, entity, label.toLowerCase()));
                        //add response to list of responses
                        requests.add(request);
                        break;
                }
            } catch (Throwable typeNotFound) {
                //log error
            }
        });

        return requests;
    }

    /**
     * generate specific entity type mapping
     *
     * @param entityType
     * @param ent
     * @param label
     * @return
     */
    public Map<String, Object> generateMapping(EntityType entityType, Entity ent, String label) {
        Optional<EntityType> entity = ontology.entity(entityType.getName());
        if (!entity.isPresent())
            throw new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No entity with name " + label + " found in ontology"));

        Map<String, Object> jsonMap = new HashMap<>();
        //populate index fields
        jsonMap.put(label, populateMappingIndexFields(ent, entity));

        return jsonMap;
    }

    public Map<String, Object> populateMappingIndexFields(Entity ent, Optional<EntityType> entity) {
        Map<String, Object> mapping = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        mapping.put(PROPERTIES, properties);
        //populate fields & metadata
        EntityType entityType = entity.get();
        populateProperty(ent, properties, entityType);
        return mapping;
    }

    public void populateProperty(Entity ent, Map<String, Object> properties, EntityType entityType) {
        entityType.getMetadata().forEach(v -> properties.put(v, parseType(ontology.property$(v).getType())));
        entityType.getProperties().forEach(v -> properties.put(v, parseType(ontology.property$(v).getType())));
        //populate nested documents
        ent.getNested().forEach(nest -> generateNestedEntMapping(properties, nest));
    }

    /**
     * generate specific relation type mapping
     *
     * @param relationshipType
     * @param label
     * @return
     */
    public Map<String, Object> generateMapping(RelationshipType relationshipType, Relation rel, String label) {
        Optional<RelationshipType> relation = ontology.relation(relationshipType.getName());
        if (!relation.isPresent())
            throw new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No relation    with name " + label + " found in ontology"));

        Map<String, Object> jsonMap = new HashMap<>();

        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> mapping = new HashMap<>();
        mapping.put(PROPERTIES, properties);
        //populate fields & metadata
        relation.get().getMetadata().forEach(v -> properties.put(v, parseType(ontology.property$(v).getType())));
        relation.get().getProperties().forEach(v -> properties.put(v, parseType(ontology.property$(v).getType())));
        //set direction
        properties.put(DIRECTION, parseType("string"));
        //populate  sideA (entityA)
        populateRedundand(ENTITY_A, relationshipType.getName(), properties);
        //populate  sideB (entityB)
        populateRedundand(ENTITY_B, relationshipType.getName(), properties);
        //populate nested documents
        rel.getNested().forEach(nest -> generateNestedRelMapping(properties, nest));

        jsonMap.put(label, mapping);
        return jsonMap;
    }

    private void generateNestedRelMapping(Map<String, Object> parent, Relation nest) {
        Optional<RelationshipType> relation = ontology.relation(nest.getType());
        if (!relation.isPresent())
            throw new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No relation with name " + nest.getType() + " found in ontology"));

        Map<String, Object> mapping = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        switch (nest.getMapping()) {
            case EMBEDDED:
                //no specific mapping here -
                break;
            case CHILD:
                mapping.put(TYPE, NESTED);
                break;
        }
        mapping.put(PROPERTIES, properties);
        //populate fields & metadata
        relation.get().getMetadata().forEach(v -> properties.put(v, parseType(ontology.property$(v).getType())));
        relation.get().getProperties().forEach(v -> properties.put(v, parseType(ontology.property$(v).getType())));

        //inner child nested population
        nest.getNested().forEach(inner -> generateNestedRelMapping(properties, inner));
        //assuming single value exists (this is the field name)
        String nestedName = nest.getProps().getValues().get(0);
        parent.put(nestedName, mapping);

    }

    private void generateNestedEntMapping(Map<String, Object> parent, Entity nest) {
        Optional<EntityType> entity = ontology.entity(nest.getType());
        if (!entity.isPresent())
            throw new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No entity with name " + nest.getType() + " found in ontology"));

        Map<String, Object> mapping = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        switch (nest.getMapping()) {
            case EMBEDDED:
                //no specific mapping here -
                break;
            case CHILD:
                mapping.put(TYPE, NESTED);
                break;
        }
        mapping.put(PROPERTIES, properties);
        //populate fields & metadata
        populateProperty(nest, properties, entity.get());
        //assuming single value exists (this is the field name)
        if (nest.getProps().getValues().isEmpty())
            throw new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "Nested entity with name " + nest.getType() + " has no property value in mapping file"));

        String nestedName = nest.getProps().getValues().get(0);
        parent.put(nestedName, mapping);
    }

    private void populateRedundand(String side, String label, Map<String, Object> properties) {
        HashMap<String, Object> sideProperties = new HashMap<>();
        properties.put(side, sideProperties);
        HashMap<String, Object> values = new HashMap<>();
        sideProperties.put(PROPERTIES, values);

        //add side ID
        values.put(ID, parseType(ontology.property$(ID).getType()));
        //add side TYPE
        values.put(TYPE, parseType(ontology.property$(TYPE).getType()));
        indexProvider.getRelation(label).get().getRedundant(side)
                .forEach(r -> values.put(r.getName(), parseType(ontology.property$(r.getName()).getType())));
    }

    /**
     * parse ontology primitive type to elastic primitive type
     *
     * @param nameType
     * @return
     */
    private Map<String, Object> parseType(String nameType) {
        Map<String, Object> map = new HashMap<>();
        try {
            OntologyPrimitiveType type = OntologyPrimitiveType.valueOf(nameType.toUpperCase());
            switch (type) {
                case STRING:
                    map.put("type", "keyword");
                    break;
                case TEXT:
                    map.put("type", "text");
                    map.put("fields", singletonMap("keyword", singletonMap("type", "keyword")));
                    break;
                case DATE:
                    map.put("type", "date");
                    map.put("format", "epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS");
                    break;
                case LONG:
                    map.put("type", "long");
                    break;
                case INT:
                    map.put("type", "integer");
                    break;
                case FLOAT:
                    map.put("type", "float");
                    break;
                case DOUBLE:
                    map.put("type", "double");
                    break;
                case GEO:
                    map.put("type", "geo_point");
                    break;
            }
        } catch (Throwable typeNotFound) {
            //log type not found
        }
        return map;
    }

    /**
     * add the index entity settings part of the template according to the ontology relations
     *
     * @return
     */
    public Settings generateSettings(EntityType entityType, Entity entity, String label) {
        if (!ontology.entity(entityType.getName()).get().getMetadata().contains("id"))
            throw new FuseError.FuseErrorException(new FuseError("Schema generation exception", " Entity " + label + " not containing id metadata property "));

        // TODO: 05/12/2019  - use index provider to correctly build index settings
        return builder(entity);
    }

    /**
     * add the index relation settings part of the template according to the ontology relations
     *
     * @return
     */
    public Settings generateSettings(RelationshipType relationType, Relation rel, String label) {
        if (!ontology.relation(relationType.getName()).get().getMetadata().contains("id"))
            throw new FuseError.FuseErrorException(new FuseError("Schema generation exception", " Relationship " + label + " not containing id metadata property "));

        return builder(rel);
    }

    private Settings builder(Relation relation) {
        Settings.Builder builder = getSettings();
        if (relation.getNested().isEmpty()) {
            //assuming id is a mandatory part of metadata/properties
            builder.put("sort.field", "id")
                    .put("sort.order", "asc");
        }
        return builder.build();
    }

    private Settings builder(Entity entity) {
        Settings.Builder builder = getSettings();
        if (entity.getNested().isEmpty()) {
            //assuming id is a mandatory part of metadata/properties
            builder.put("sort.field", "id")
                    .put("sort.order", "asc");
        }
        return builder.build();
    }

    private Settings.Builder getSettings() {
        return Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 1);
    }
}
