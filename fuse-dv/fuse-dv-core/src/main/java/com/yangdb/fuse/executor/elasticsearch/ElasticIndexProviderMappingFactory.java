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
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.Entity;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.model.schema.Relation;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.Tuple2;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
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
        StreamSupport.stream(ontology.relations().spliterator(), false).forEach(r -> {
            CreateIndexRequest request = new CreateIndexRequest(r.getName().toLowerCase());
            responses.add(client.admin().indices().create(request).actionGet());
        });

        StreamSupport.stream(ontology.entities().spliterator(), false).forEach(e -> {
            CreateIndexRequest request = new CreateIndexRequest(e.getName().toLowerCase());
            responses.add(client.admin().indices().create(request).actionGet());
        });

        return responses.stream().map(CreateIndexResponse::index).collect(Collectors.toList());
    }

    public List<Tuple2<String, Boolean>> generateMappings() {
        List<Tuple2<String, AcknowledgedResponse>> responses = new ArrayList<>();
        responses.addAll(mapEntities());
        responses.addAll(mapRelations());
        return responses.stream().map(r -> new Tuple2<>(r._1, r._2.isAcknowledged())).collect(Collectors.toList());
    }

    /**
     * add the mapping part of the template according to the ontology relations
     *
     * @return
     */
    private List<Tuple2<String, AcknowledgedResponse>> mapRelations() {
        List<Tuple2<String, AcknowledgedResponse>> responses = new ArrayList<>();
        ontology.relations().forEach(r -> {
            String mapping = indexProvider.getRelation(r.getName()).orElseThrow(
                    () -> new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No entity with name " + r + " found in ontology")))
                    .getPartition();

            Relation relation = indexProvider.getRelation(r.getName()).get();
            switch (mapping) {
                case "static":
                    //static index
                    relation.getProps().getValues().forEach(v -> {
                        String label = r.getrType();
                        PutIndexTemplateRequest request = new PutIndexTemplateRequest(label.toLowerCase());
                        request.patterns(Arrays.asList(r.getName().toLowerCase(),label, r.getName(), String.format("%s%s", v, "*")))
                                .settings(generateSettings(r, label))
                                .mapping(label, generateMapping(r, label));
                        //add response to list of responses
                        responses.add(new Tuple2<>(v, client.admin().indices().putTemplate(request).actionGet()));
                    });
                    break;
                case "time":
                    String label = r.getrType();
                    PutIndexTemplateRequest request = new PutIndexTemplateRequest(relation.getType().toLowerCase());
                    request.patterns(Arrays.asList(r.getName().toLowerCase(),label, r.getName(), String.format(relation.getProps().getIndexFormat(), "*")))
                            .settings(generateSettings(r, label))
                            .mapping(label, generateMapping(r, label));
                    //add response to list of responses
                    responses.add(new Tuple2<>(label.toLowerCase(), client.admin().indices().putTemplate(request).actionGet()));
                    break;
            }
        });
        return responses;
    }

    /**
     * add the mapping part of the template according to the ontology entities
     *
     * @return
     */
    private List<Tuple2<String, AcknowledgedResponse>> mapEntities() {
        List<Tuple2<String, AcknowledgedResponse>> responses = new ArrayList<>();
        StreamSupport.stream(ontology.entities().spliterator(), false).forEach(e -> {
            String mapping = indexProvider.getEntity(e.getName()).orElseThrow(
                    () -> new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No entity with name " + e + " found in ontology")))
                    .getPartition();

            Entity entity = indexProvider.getEntity(e.getName()).get();
            switch (mapping) {
                case "static":
                    //static index
                    entity.getProps().getValues().forEach(v -> {
                        String label = e.geteType();
                        PutIndexTemplateRequest request = new PutIndexTemplateRequest(v.toLowerCase());
                        request.patterns(Arrays.asList(e.getName().toLowerCase(),label, e.getName(), String.format("%s%s", v, "*")))
                                .settings(generateSettings(e, label))
                                .mapping(label, generateMapping(e, label));
                        //add response to list of responses
                        responses.add(new Tuple2<>(label.toLowerCase(), client.admin().indices().putTemplate(request).actionGet()));
                    });
                    break;
                case "time":
                    PutIndexTemplateRequest request = new PutIndexTemplateRequest(e.getName().toLowerCase());
                    String label = entity.getType();
                    request.patterns(Arrays.asList(e.getName().toLowerCase(), label, e.getName(), String.format(entity.getProps().getIndexFormat(), "*")))
                            .settings(generateSettings(e, label))
                            .mapping(label, generateMapping(e, label.toLowerCase()));
                    //add response to list of responses
                    responses.add(new Tuple2<>(label.toLowerCase(), client.admin().indices().putTemplate(request).actionGet()));
                    break;
            }
        });
        return responses;
    }

    /**
     * generate specific entity type mapping
     *
     * @param entityType
     * @param label
     * @return
     */
    public Map<String, Object> generateMapping(EntityType entityType, String label) {
        Optional<EntityType> entity = ontology.entity(entityType.getName());
        if (!entity.isPresent())
            throw new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No entity with name " + label + " found in ontology"));

        Map<String, Object> jsonMap = new HashMap<>();

        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        //populate fields & metadata
        entity.get().getMetadata().forEach(v -> properties.put(v, parseType(ontology.property$(v).getType())));
        entity.get().getProperties().forEach(v -> properties.put(v, parseType(ontology.property$(v).getType())));
        jsonMap.put(label, mapping);

        return jsonMap;
    }

    /**
     * generate specific relation type mapping
     *
     * @param relationshipType
     * @param label
     * @return
     */
    public Map<String, Object> generateMapping(RelationshipType relationshipType,String label) {
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
        properties.put(DIRECTION,parseType("string"));
        //populate  sideA (entityA)
        populateRedundand(ENTITY_A,relationshipType.getName(), properties);
        //populate  sideB (entityB)
        populateRedundand(ENTITY_B,relationshipType.getName(), properties);

        jsonMap.put(label, mapping);
        return jsonMap;
    }

    private void populateRedundand(String side,String label, Map<String, Object> properties) {
        HashMap<String, Object> sideProperties = new HashMap<>();
        properties.put(side, sideProperties);
        HashMap<String, Object> values = new HashMap<>();
        sideProperties.put(PROPERTIES, values);

        //add side ID
        values.put(ID,parseType(ontology.property$(ID).getType()));
        //add side TYPE
        values.put(TYPE,parseType(ontology.property$(TYPE).getType()));
        indexProvider.getRelation(label).get().getRedundant(side)
                .forEach(r-> values.put(r.getName(),parseType(ontology.property$(r.getName()).getType())));
    }

    /**
     * parse ontology primitive type to elastic primitive type
     *
     * @param type
     * @return
     */
    private Map<String, Object> parseType(String type) {
        Map<String, Object> map = new HashMap<>();
        switch (type) {
            case "string":
                map.put("type", "keyword");
                break;
            case "text":
                map.put("type", "text");
                map.put("fields", singletonMap("keyword", singletonMap("type", "keyword")));
                break;
            case "date":
                map.put("type", "date");
                map.put("format", "epoch_millis||strict_date_optional_time||yyyy-MM-dd HH:mm:ss.SSS");
                break;
            case "long":
                map.put("type", "long");
                break;
            case "int":
                map.put("type", "integer");
                break;
            case "float":
                map.put("type", "float");
                break;
            case "double":
                map.put("type", "double");
                break;
            case "geo":
                map.put("type", "geo_point");
                break;
        }
        return map;
    }

    /**
     * add the index entity settings part of the template according to the ontology relations
     *
     * @return
     */
    public Settings generateSettings(EntityType entityType, String label) {

        if (!ontology.entity(entityType.getName()).get().getMetadata().contains("id"))
            throw new FuseError.FuseErrorException(new FuseError("Schema generation exception", " Entity " + label + " not containing id metadata property "));

        return builder();
    }

    /**
     * add the index relation settings part of the template according to the ontology relations
     *
     * @return
     */
    public Settings generateSettings(RelationshipType relationType, String label) {

        if (!ontology.relation(relationType.getName()).get().getMetadata().contains("id"))
            throw new FuseError.FuseErrorException(new FuseError("Schema generation exception", " Relationship " + label + " not containing id metadata property "));

        return builder();
    }

    private Settings builder() {
        return Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 1)
                //assuming id is a mandatory part of metadata/properties
                .put("sort.field", "id")
                .put("sort.order", "asc")
                .build();
    }
}
