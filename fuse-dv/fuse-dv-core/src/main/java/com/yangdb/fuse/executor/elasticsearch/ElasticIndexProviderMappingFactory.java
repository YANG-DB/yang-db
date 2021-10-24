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
import com.yangdb.fuse.executor.ontology.schema.OntologyIndexGenerator;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.ontology.*;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.*;
import javaslang.Tuple2;
import org.opensearch.ResourceAlreadyExistsException;
import org.opensearch.action.admin.indices.create.CreateIndexRequest;
import org.opensearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.Client;
import org.opensearch.common.settings.Settings;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.model.GlobalConstants.ProjectionConfigs.*;
import static java.util.Collections.singletonMap;

/**
 * generate elastic mapping template according to ontology and index provider schema
 * generate the indices according to the index provider partitions
 */
public class ElasticIndexProviderMappingFactory implements OntologyIndexGenerator {

    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String DIRECTION = GlobalConstants.EdgeSchema.DIRECTION;
    public static final String ENTITY_A = GlobalConstants.EdgeSchema.SOURCE;
    public static final String ENTITY_B = GlobalConstants.EdgeSchema.DEST;
    public static final String PROPERTIES = "properties";
    public static final String NESTED = "nested";
    public static final String CHILD = "child";
    public static final String EMBEDDED = "embedded";

    private Client client;
    private RawSchema schema;
    private IndexProvider indexProvider;
    private Ontology ontology;

    @Inject
    public ElasticIndexProviderMappingFactory(Client client, RawSchema schema, Ontology ontology, IndexProvider indexProvider) {
        this.client = client;
        this.schema = schema;
        this.indexProvider = indexProvider;
        this.ontology = ontology;
    }

    public ElasticIndexProviderMappingFactory indexProvider(IndexProvider indexProvider) {
        this.indexProvider = indexProvider;
        return this;
    }

    public ElasticIndexProviderMappingFactory ontology(Ontology ontology) {
        this.ontology = ontology;
        return this;
    }

    /**
     * create indices according to ontology
     *
     * @return
     */
    public List<Tuple2<Boolean, String>> createIndices() {
        List<Tuple2<Boolean, String>> responses = new ArrayList<>();
        StreamSupport.stream(schema.indices().spliterator(), false).forEach(name -> {
            try {
                CreateIndexRequest request = new CreateIndexRequest(name);
                responses.add(new Tuple2<>(true, client.admin().indices().create(request).actionGet().index()));
            } catch (ResourceAlreadyExistsException e) {
                responses.add(new Tuple2<>(false, e.getIndex().getName()));
            } catch (Throwable t) {
                throw new FuseError.FuseErrorException("Error Generating Indices for E/S ", t);
            }
        });
        return responses;
    }

    /**
     * generate mapping according to ontology
     *
     * @return
     */
    public List<Tuple2<String, Boolean>> generateMappings() {
        List<Tuple2<String, AcknowledgedResponse>> responses = new ArrayList<>();
        try {
            //generate the index template requests
            Map<String, ESPutIndexTemplateRequestBuilder> requests = new HashMap<>();
            Ontology.Accessor ontology = new Ontology.Accessor(this.ontology);
            mapEntities(ontology, client, requests);
            mapRelations(ontology, client, requests);
            //map the special projection index
            mapProjection(generateProjectionOntology(this.ontology), client, requests);
            //execute template requesst
            responses.addAll(requests.values().stream()
                    .map(r -> new Tuple2<>(r.request().name(), r.execute().actionGet()))
                    .collect(Collectors.toList()));
            return responses.stream().map(r -> new Tuple2<>(r._1, r._2.isAcknowledged()))
                    .collect(Collectors.toList());
        } catch (Throwable t) {
            throw new FuseError.FuseErrorException("Error Generating Mapping for E/S ", t);
        }
    }

    /**
     * wrap entities with projection related metadata fields for the purpose of the projection index mapping creation
     *
     * @param ontology
     * @return
     */
    private Ontology.Accessor generateProjectionOntology(Ontology ontology) {
        //adding projection related metadata
        Ontology clone = new Ontology(ontology);
        //add projection related metadata
        clone.getEntityTypes().forEach(e -> e.withMetadata(Collections.singletonList("tag")));
        clone.getRelationshipTypes().forEach(r -> r.withMetadata(Collections.singletonList("tag")));
        clone.getRelationshipTypes().forEach(r -> r.withMetadata(Collections.singletonList(GlobalConstants.EdgeSchema.DEST_TYPE)));
        clone.getRelationshipTypes().forEach(r -> r.withMetadata(Collections.singletonList(GlobalConstants.EdgeSchema.DEST_ID)));

        clone.getProperties().add(new Property("tag", "tag", "string"));
        clone.getProperties().add(new Property(GlobalConstants.EdgeSchema.DEST_TYPE, GlobalConstants.EdgeSchema.DEST_TYPE, "string"));
        clone.getProperties().add(new Property(GlobalConstants.EdgeSchema.DEST_ID, GlobalConstants.EdgeSchema.DEST_ID, "string"));
        return new Ontology.Accessor(clone);
    }


    /**
     * add the mapping part of the template according to the ontology relations
     *
     * @return
     */
    private Collection<ESPutIndexTemplateRequestBuilder> mapRelations(Ontology.Accessor ontology, Client client, Map<String, ESPutIndexTemplateRequestBuilder> requests) {
        ontology.relations().forEach(r -> {
            String mapping = indexProvider.getRelation(r.getName()).orElseThrow(
                    () -> new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No entity with name " + r + " found in ontology")))
                    .getPartition();

            Relation relation = indexProvider.getRelation(r.getName()).get();
            MappingIndexType type = MappingIndexType.valueOf(mapping.toUpperCase());
            switch (type) {
                case NESTED:
                    //this is implement in the populateNested() method
                    break;
                case UNIFIED:
                    //common general index - unifies all entities under the same physical index
                    relation.getProps().getValues().forEach(v -> {
                        String label = r.getrType();
                        String unifiedName = relation.getProps().getValues().isEmpty() ? label : relation.getProps().getValues().get(0);
                        ESPutIndexTemplateRequestBuilder request = requests.computeIfAbsent(unifiedName, s -> new ESPutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, unifiedName));

                        List<String> patterns = new ArrayList<>(Arrays.asList(r.getName().toLowerCase(), label, r.getName(), String.format("%s%s", v, "*")));
                        if (Objects.isNull(request.request().patterns())) {
                            request.setPatterns(new ArrayList<>(patterns));
                        } else {
                            request.request().patterns().addAll(patterns);
                        }
                        //dedup patterns
                        request.setPatterns(request.request().patterns().stream().distinct().collect(Collectors.toList()));

                        //no specific index sort order since it contains multiple entity types -
                        if (request.request().settings().isEmpty()) {
                            request.setSettings(getSettings());
                        }
                        //create new mapping only when no prior entity set this mapping before
                        if (request.request().mappings().isEmpty()) {
                            request.addMapping(unifiedName, generateRelationMapping(ontology, r, relation, unifiedName));
                        } else {
                            populateProperty(ontology, relation, request.getMappingsProperties(unifiedName), r);
                        }
                    });
                    break;
                case STATIC:
                    //static index
                    relation.getProps().getValues().forEach(v -> {
                        String label = r.getrType();
                        ESPutIndexTemplateRequestBuilder request = new ESPutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, label.toLowerCase());
                        request.setPatterns(new ArrayList<>(Arrays.asList(r.getName().toLowerCase(), label, r.getName(), String.format("%s%s", v, "*"))))
                                .setSettings(generateSettings(ontology, r, relation, label))
                                .addMapping(label, generateRelationMapping(ontology, r, relation, label));
                        //dedup patterns -
                        request.setPatterns(request.request().patterns().stream().distinct().collect(Collectors.toList()));
                        //add response to list of responses
                        requests.put(label.toLowerCase(), request);
                    });
                    break;
                case TIME:
                    String label = r.getrType();
                    ESPutIndexTemplateRequestBuilder request = new ESPutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, relation.getType().toLowerCase());
                    //todo - Only the time based partition will have a template suffix with astrix added to allow numbering and dates as part of the naming convention
                    request.setPatterns(new ArrayList<>(Arrays.asList(r.getName().toLowerCase(), label, r.getName(), String.format(relation.getProps().getIndexFormat(), "*"))))
                            .setSettings(generateSettings(ontology, r, relation, label))
                            .addMapping(label, generateRelationMapping(ontology, r, relation, label));
                    //add response to list of responses

                    //dedup patterns -
                    request.setPatterns(request.request().patterns().stream().distinct().collect(Collectors.toList()));

                    //add the request
                    requests.put(relation.getType(), request);
                    break;
                default:
                    String result = "No mapping found";
                    break;
            }
        });
        return requests.values();
    }

    /**
     * add the mapping part of the template according to the ontology
     * This projection mapping is a single unified index containing the entire ontology wrapped into a single index so that
     * every type of query result can be indexed and queried for slice & dice type of questions
     * <p>
     * "properties": {
     *   "entityA": {
     *     "type": "nested",
     *     "properties": {
     *       "entityA_id": {
     *         "type": "integer",
     *       },
     *       "relationA": {
     *         "type": "nested",
     *         "properties": {
     *           "relationA_id": {
     *             "type": "integer",
     *           }
     *         }
     *       }
     *     }
     *   },
     *   "entityB": {
     *     "type": "nested",
     *     "properties": {
     *       "entityB_id": {
     *         "type": "integer",
     *       },
     *       "relationB": {
     *         "type": "nested",
     *         "properties": {
     *           "relationB_id": {
     *             "type": "integer",
     *           }
     *         }
     *       }
     *     }
     *   }
     *   }
     *
     * @param client
     * @return
     */
    private void mapProjection(Ontology.Accessor ontology, Client client, Map<String, ESPutIndexTemplateRequestBuilder> requests) {
        ESPutIndexTemplateRequestBuilder request = new ESPutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, "projection");
        request.setSettings(getSettings())
                .setPatterns(Collections.singletonList(String.format("%s*",PROJECTION)));

        Map<String, Object> jsonMap = new HashMap<>();
        Map<String, Object> rootMapping = new HashMap<>();
        Map<String, Object> rootProperties = new HashMap<>();
        rootMapping.put(PROPERTIES, rootProperties);

        //populate the query id
        rootProperties.put(QUERY_ID, parseType(ontology, "string"));
        rootProperties.put(CURSOR_ID, parseType(ontology, "string"));
        rootProperties.put(EXECUTION_TIME, parseType(ontology, "date"));
        //populate index fields
        jsonMap.put(PROJECTION, rootMapping);

        IndexProvider projection = new IndexProvider(this.indexProvider);
        //remove nested entities since we upgraded them to the root level
        projection.getEntities().forEach(e -> e.getNested().clear());

        projection.getEntities()
                .forEach(entity -> {
                    //todo remove nested entities since they already appear as a qualified ontological entity
                    try {
                        //generate entity mapping - each entity should be a nested objects array
                        Map<String, Object> objectMap = generateNestedEntityMapping(ontology, rootProperties, entity.withMapping(CHILD));
                        //generate relation mapping - each entity's relation should be a nested objects array inside the entity
                        List<RelationshipType> relationshipTypes = ontology.relationBySideA(entity.getType());
                        relationshipTypes.forEach(rel -> {
                            Relation relation = this.indexProvider.getRelation(rel.getName()).get();
                            generateNestedRelationMapping(ontology, (Map<String, Object>) objectMap.get(PROPERTIES), relation.withMapping(CHILD));
                        });
                    } catch (Throwable typeNotFound) {
                        //log error
                    }
                });
        request.addMapping(PROJECTION, jsonMap);
        //add response to list of responses
        requests.put(PROJECTION, request);

    }

    /**
     * add the mapping part of the template according to the ontology entities
     *
     * @param client
     * @return
     */
    private Collection<ESPutIndexTemplateRequestBuilder> mapEntities(Ontology.Accessor ontology, Client client, Map<String, ESPutIndexTemplateRequestBuilder> requests) {
        StreamSupport.stream(ontology.entities().spliterator(), false)
                .forEach(e -> {
                    String mapping = indexProvider.getEntity(e.getName()).orElseThrow(
                            () -> new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No entity with name " + e + " found in ontology")))
                            .getPartition();

                    Entity entity = indexProvider.getEntity(e.getName()).get();
                    try {
                        MappingIndexType type = MappingIndexType.valueOf(mapping.toUpperCase());
                        switch (type) {
                            case NESTED:
                                //this is implement in the populateNested() method
                                break;
                            case UNIFIED:
                                //common general index - unifies all entities under the same physical index
                                entity.getProps().getValues().forEach(v -> {
                                    String label = e.geteType();
                                    String unifiedName = entity.getProps().getValues().isEmpty() ? label : entity.getProps().getValues().get(0);
                                    ESPutIndexTemplateRequestBuilder request = requests.computeIfAbsent(unifiedName, s -> new ESPutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, unifiedName));

                                    List<String> patterns = new ArrayList<>(Arrays.asList(e.getName().toLowerCase(), label, e.getName(), String.format("%s%s", v, "*")));
                                    if (Objects.isNull(request.request().patterns())) {
                                        request.setPatterns(new ArrayList<>(patterns));
                                    } else {
                                        request.request().patterns().addAll(patterns);
                                    }
                                    //dedup patterns -
                                    request.setPatterns(request.request().patterns().stream().distinct().collect(Collectors.toList()));
                                    //no specific index sort order since it contains multiple entity types -
                                    if (request.request().settings().isEmpty()) {
                                        request.setSettings(getSettings());
                                    }
                                    //create new mapping only when no prior entity set this mapping before
                                    if (request.request().mappings().isEmpty()) {
                                        request.addMapping(unifiedName, generateEntityMapping(ontology, e, entity, unifiedName));
                                    } else {
                                        populateProperty(ontology, entity, request.getMappingsProperties(unifiedName), e);
                                    }
                                });
                                break;
                            case STATIC:
                                //static index
                                entity.getProps().getValues().forEach(v -> {
                                    String label = e.geteType();
                                    ESPutIndexTemplateRequestBuilder request = new ESPutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, v.toLowerCase());
                                    request.setPatterns(new ArrayList<>(Arrays.asList(e.getName().toLowerCase(), label, e.getName(), String.format("%s%s", v, "*"))))
                                            .setSettings(generateSettings(ontology, e, entity, label))
                                            .addMapping(label, generateEntityMapping(ontology, e, entity, label));

                                    //dedup patterns -
                                    request.setPatterns(request.request().patterns().stream().distinct().collect(Collectors.toList()));

                                    //add response to list of responses
                                    requests.put(v.toLowerCase(), request);
                                });
                                break;
                            case TIME:
                                //time partitioned index
                                ESPutIndexTemplateRequestBuilder request = new ESPutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, e.getName().toLowerCase());
                                String label = entity.getType();
                                request.setPatterns(new ArrayList<>(Arrays.asList(e.getName().toLowerCase(), label, e.getName(), String.format(entity.getProps().getIndexFormat(), "*"))))
                                        .setSettings(generateSettings(ontology, e, entity, label))
                                        .addMapping(label, generateEntityMapping(ontology, e, entity, label.toLowerCase()));
                                //dedup patterns -
                                request.setPatterns(request.request().patterns().stream().distinct().collect(Collectors.toList()));

                                //add response to list of responses

                                requests.put(e.getName().toLowerCase(), request);
                                break;
                        }
                    } catch (Throwable typeNotFound) {
                        //log error
                    }
                });

        return requests.values();
    }


    public Map<String, Object> populateMappingIndexFields(Ontology.Accessor ontology, Entity ent, Optional<EntityType> entity) {
        Map<String, Object> mapping = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        mapping.put(PROPERTIES, properties);
        //populate fields & metadata
        EntityType entityType = entity.get();

        //generate field id -> only if field id array size > 1
        if (entityType.getIdField().size() > 1) {
            properties.put(entityType.idFieldName(), Collections.singletonMap("type", "keyword"));
        }//otherwise that field id is already a part of the regular fields

        populateProperty(ontology, ent, properties, entityType);
        return mapping;
    }

    public void populateProperty(Ontology.Accessor ontology, BaseTypeElement<? extends BaseTypeElement> element, Map<String, Object> properties, BaseElement entityType) {
        entityType.getMetadata().forEach(v -> {
            Map<String, Object> parseType = parseType(ontology, ontology.property$(v).getType());
            if (!parseType.isEmpty()) properties.put(v, parseType);
        });
        entityType.getProperties().forEach(v -> {
            Map<String, Object> parseType = parseType(ontology, ontology.property$(v).getType());
            if (!parseType.isEmpty()) properties.put(v, parseType);
        });
        //populate nested documents
        populateNested(ontology, element, properties);
    }

    public void populateNested(Ontology.Accessor ontology, BaseTypeElement<? extends BaseTypeElement> element, Map<String, Object> properties) {
        element.getNested().forEach(nest -> generateNestedEntityMapping(ontology, properties, nest));
    }

    /**
     * generate specific entity type mapping
     *
     * @param entityType
     * @param ent
     * @param label
     * @return
     */
    public Map<String, Object> generateEntityMapping(Ontology.Accessor ontology, EntityType entityType, Entity ent, String label) {
        Optional<EntityType> entity = ontology.entity(entityType.getName());
        if (!entity.isPresent())
            throw new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No entity with name " + label + " found in ontology"));

        Map<String, Object> jsonMap = new HashMap<>();
        //populate index fields
        jsonMap.put(label, populateMappingIndexFields(ontology, ent, entity));

        return jsonMap;
    }

    /**
     * generate specific relation type mapping
     *
     * @param relationshipType
     * @param label
     * @return
     */
    public Map<String, Object> generateRelationMapping(Ontology.Accessor ontology, RelationshipType relationshipType, Relation rel, String label) {
        Optional<RelationshipType> relation = ontology.relation(relationshipType.getName());
        if (!relation.isPresent())
            throw new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No relation    with name " + label + " found in ontology"));

        Map<String, Object> jsonMap = new HashMap<>();

        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> mapping = new HashMap<>();
        mapping.put(PROPERTIES, properties);

        //generate field id -> only if field id array size > 1
        if (relationshipType.getIdField().size() > 1) {
            properties.put(relationshipType.idFieldName(), Collections.singletonMap("type", "keyword"));
        }//otherwise that field id is already a part of the regular fields


        //populate fields & metadata
        relation.get().getMetadata().forEach(v -> properties.put(v, parseType(ontology, ontology.property$(v).getType())));
        relation.get().getProperties().forEach(v -> properties.put(v, parseType(ontology, ontology.property$(v).getType())));
        //set direction
        properties.put(DIRECTION, parseType(ontology, "string"));
        //populate  sideA (entityA)
        populateRedundand(ontology, ENTITY_A, relationshipType.getName(), properties);
        //populate  sideB (entityB)
        populateRedundand(ontology, ENTITY_B, relationshipType.getName(), properties);
        //populate nested documents
        rel.getNested().forEach(nest -> generateNestedRelationMapping(ontology, properties, nest));

        //add mapping only if properties size > 0
        if (properties.size() > 0) {
            jsonMap.put(label, mapping);
        }
        return jsonMap;
    }

    private void generateNestedRelationMapping(Ontology.Accessor ontology, Map<String, Object> parent, BaseTypeElement<? extends BaseTypeElement> nest) {
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
        populateProperty(ontology, nest, properties, relation.get());
        //assuming single value exists (this is the field name)
        if (nest.getProps().getValues().isEmpty())
            throw new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "Nested Rel with name " + nest.getType() + " has no property value in mapping file"));

        //inner child nested population
        nest.getNested().forEach(inner -> generateNestedRelationMapping(ontology, properties, inner));
        //assuming single value exists (this is the field name)
        //add mapping only if properties size > 0
        if (properties.size() > 0) {
            parent.put(nest.getType(), mapping);
        }
    }

    private Map<String, Object> generateNestedEntityMapping(Ontology.Accessor ontology, Map<String, Object> parent, BaseTypeElement<? extends BaseTypeElement> nest) {
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
        populateProperty(ontology, nest, properties, entity.get());
        //assuming single value exists (this is the field name)
        if (nest.getProps().getValues().isEmpty())
            throw new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "Nested entity with name " + nest.getType() + " has no property value in mapping file"));

        //add mapping only if properties size > 0
        if (properties.size() > 0) {
            parent.put(nest.getType(), mapping);
        }
        return mapping;
    }

    private void populateRedundand(Ontology.Accessor ontology, String side, String label, Map<String, Object> properties) {
        HashMap<String, Object> sideProperties = new HashMap<>();
        properties.put(side, sideProperties);
        HashMap<String, Object> values = new HashMap<>();
        sideProperties.put(PROPERTIES, values);

        //add side ID
        values.put(ID, parseType(ontology, ontology.property$(ID).getType()));
        //add side TYPE
        values.put(TYPE, parseType(ontology, ontology.property$(TYPE).getType()));
        indexProvider.getRelation(label).get().getRedundant(side)
                .forEach(r -> values.put(r.getName(), parseType(ontology, ontology.property$(r.getName()).getType())));
    }

    /**
     * parse ontology primitive type to elastic primitive type
     *
     * @param nameType
     * @return
     */
    private Map<String, Object> parseType(Ontology.Accessor ontology, String nameType) {
        Map<String, Object> map = new HashMap<>();
        try {
            Ontology.OntologyPrimitiveType type = Ontology.OntologyPrimitiveType.valueOf(nameType.toUpperCase());
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
            // manage non-primitive type such as enum or nested typed
            Optional<Tuple2<Ontology.Accessor.NodeType, String>> type = ontology.matchNameToType(nameType);
            if (type.isPresent()) {
                switch (type.get()._1()) {
                    case ENTITY:
                        //todo - manage the nested-embedded type here
                        break;
                    case ENUM:
                        //enum is always backed by integer
                        map.put("type", "integer");
                        break;
                    case RELATION:
                        break;
                }
            } else {
                //default
                map.put("type", "text");
                map.put("fields", singletonMap("keyword", singletonMap("type", "keyword")));
            }
        }
        return map;
    }

    /**
     * add the index entity settings part of the template according to the ontology relations
     *
     * @return
     */
    public Settings generateSettings(Ontology.Accessor ontology, EntityType entityType, Entity entity, String label) {
        ontology.entity(entityType.getName()).get().getIdField().forEach(idField -> {
            if (!ontology.entity(entityType.getName()).get().fields().contains(idField))
                throw new FuseError.FuseErrorException(new FuseError("Entity Schema generation exception", " Entity " + label + " not containing id metadata property "));
        });
        // TODO: 05/12/2019  - use index provider to correctly build index settings
        return builder(ontology, entity);
    }

    /**
     * add the index relation settings part of the template according to the ontology relations
     *
     * @return
     */
    public Settings generateSettings(Ontology.Accessor ontology, RelationshipType relationType, Relation rel, String label) {
        ontology.relation(relationType.getName()).get().getIdField().forEach(idField -> {
            if (!ontology.relation(relationType.getName()).get().fields().contains(idField))
                throw new FuseError.FuseErrorException(new FuseError("Relation Schema generation exception", " Relationship " + label + " not containing id metadata property "));
        });
        return builder(ontology, rel);
    }

    private Settings builder(Ontology.Accessor ontology, Relation relation) {
        Settings.Builder builder = getSettings();
        if (relation.getNested().isEmpty()) {
            //assuming id is a mandatory part of metadata/properties
            builder.put("sort.field", ontology.relation$(relation.getType()).idFieldName())
                    .put("sort.order", "asc");
        }
        return builder.build();
    }

    private Settings builder(Ontology.Accessor ontology, Entity entity) {
        Settings.Builder builder = getSettings();
        if (entity.getNested().isEmpty()) {
            //assuming id is a mandatory part of metadata/properties
            builder.put("sort.field", ontology.entity$(entity.getType()).idFieldName())
                    .put("sort.order", "asc");
        }
        return builder.build();
    }

    /**
     * todo - get the shards & replication params from configuration
     *
     * @return
     */
    private Settings.Builder getSettings() {
        return Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 1);
    }
}
