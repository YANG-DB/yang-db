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
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.model.ontology.*;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.*;
import com.yangdb.fuse.model.schema.implementation.relational.*;
import javaslang.Tuple2;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
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
public class ElasticImplementationProviderMappingFactory {

    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String TYPES = "types";
    public static final String DIRECTION = GlobalConstants.EdgeSchema.DIRECTION;
    public static final String ENTITY_A = GlobalConstants.EdgeSchema.SOURCE;
    public static final String ENTITY_B = GlobalConstants.EdgeSchema.DEST;
    public static final String PROPERTIES = "properties";
    public static final String NESTED = "nested";
    public static final String CHILD = "child";
    public static final String EMBEDDED = "embedded";
    public static final String _DOC = "_doc";

    private Client client;
    private RawSchema schema;
    private ImplementationLevel indexProvider;
    private Config settingConfig = ConfigFactory.empty();


    @Inject
    public ElasticImplementationProviderMappingFactory(Config config, Client client, RawSchema schema, Ontology ontology, ImplementationLevel indexProvider) {
        this.client = client;
        this.schema = schema;
        this.indexProvider = indexProvider;
        try {
            this.settingConfig = config.hasPath("elasticsearch.mappings.settings") ? config.getConfig("elasticsearch.mappings.settings") : ConfigFactory.empty();
        } catch (Throwable ignored) {
        }
    }

    public ElasticImplementationProviderMappingFactory indexProvider(ImplementationLevel indexProvider) {
        this.indexProvider = indexProvider;
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
                throw new FuseError.FuseErrorException("Error Generating Indices for E/S [" + name + "]", t);
            }
        });
        return responses;
    }

    public List<Tuple2<String, Boolean>> generateMappings() {
        List<Tuple2<String, AcknowledgedResponse>> responses = new ArrayList<>();
        try {
            //generate the index template requests
            Map<String, ESPutIndexTemplateRequestBuilder> requests = new HashMap<>();
            mapEntities(client, requests);
            mapRelations(client, requests);
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
     * add the mapping part of the template according to the ontology relations
     *
     * @return
     */
    private Collection<ESPutIndexTemplateRequestBuilder> mapRelations(Client client, Map<String, ESPutIndexTemplateRequestBuilder> requests) {
        indexProvider.getImplementationEdges().stream().forEach(e -> generateMapping(client, requests, e));
        return requests.values();
    }

    /**
     * add the mapping part of the template according to the ontology entities
     *
     * @param client
     * @return
     */
    private Collection<ESPutIndexTemplateRequestBuilder> mapEntities(Client client, Map<String, ESPutIndexTemplateRequestBuilder> requests) {
        indexProvider.getImplementationNodes().stream().forEach(e -> generateMapping(client, requests, e));
        return requests.values();
    }


    private void generateMapping(Client client, Map<String, ESPutIndexTemplateRequestBuilder> requests, ImplementationNode e) {
        e.getAttributes().forEach(v -> {
            //get or create the mapping template request
            ESPutIndexTemplateRequestBuilder request = requests.computeIfAbsent(e.getTableName(), s -> new ESPutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, e.getTableName()));

            List<String> patterns = Arrays.asList(e.getTableName(), String.format("%s%s", e.getTableName(), "*"));
            if (Objects.isNull(request.request().patterns())) {
                request.setPatterns(new ArrayList<>(patterns));
            }
            //no specific index sort order since it may contains multiple entity types -
            if (request.request().settings().isEmpty()) {
                request.setSettings(getSettings());
            }
            //create new mapping only when no prior entity set this mapping before
            if (request.request().mappings().isEmpty()) {
                request.addMapping(e.getTableName(), generateEntityMapping(e));
            } else {
                populateProperties(e, request.getMappingsProperties(e.getTableName()));
            }
        });
    }

    private void generateMapping(Client client, Map<String, ESPutIndexTemplateRequestBuilder> requests, ImplementationEdge e) {
        e.getPaths().stream().flatMap(path -> path.getTraversalHops().stream()).forEach(hop -> {
            ESPutIndexTemplateRequestBuilder request;
            //first see is this hop has a unique join table by its own
            if (!StringUtils.isEmpty(hop.getJoinTableName())) {
                //get or create the mapping template request
                request = requests.computeIfAbsent(hop.getJoinTableName(), s -> new ESPutIndexTemplateRequestBuilder(client, PutIndexTemplateAction.INSTANCE, hop.getJoinTableName()));
                //add patterns
                List<String> patterns = Arrays.asList(hop.getJoinTableName(), String.format("%s%s", hop.getJoinTableName(), "*"));
                if (Objects.isNull(request.request().patterns())) {
                    request.setPatterns(new ArrayList<>(patterns));
                }
                request.addMapping(hop.getJoinTableName(), populateMappingIndexFields(hop));
            } else {
                //source & destination tables must exist since they are part of the nodes indexes
                //todo populate sides
                request = requests.get(hop.getSourceTableName());
                //todo add destination foreign key to source node mapping

                request = requests.get(hop.getDestinationTableName());
                //todo add destination foreign key to destination node mapping

                //direction is not needed as a field since its dictated by the table which hosts the primary key & foreign key
                //add properties
                populateEdgeProperties(hop, request.getMappings());
            }
        });
    }


    public Map<String, Object> populateMappingIndexFields(ImplementationNode node) {
        Map<String, Object> mapping = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        mapping.put(PROPERTIES, properties);
        //generate general metadata fields mapping
        properties.put(TYPES, Collections.singletonMap("type", "keyword"));

        //populate fields & metadata
        populateProperties(node, properties);
        return mapping;
    }

    public Map<String, Object> populateMappingIndexFields(TraversalHop node) {
        Map<String, Object> mapping = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        mapping.put(PROPERTIES, properties);
        //generate general metadata fields mapping
        properties.put(TYPES, Collections.singletonMap("type", "keyword"));
        //set direction
        properties.put(DIRECTION, parseType("string"));

        // populate sides
        properties.put(node.getSourceTableName(), populateRedundant(node.getSourceTableColumn(), node.getRedundantSourceAttributes()));
        properties.put(node.getDestinationTableName(), populateRedundant(node.getDestinationTableColumn(), node.getRedundantSourceAttributes()));
        //add id
        properties.put(ID, Collections.singletonMap("type", "keyword"));

        //populate fields & metadata
        populateEdgeProperties(node, properties);
        return mapping;
    }

    private HashMap<String, Object> populateRedundant(String column, List<Attribute> attributes) {
        HashMap<String, Object> sideProperties = new HashMap<>();
        HashMap<String, Object> values = new HashMap<>();
        sideProperties.put(PROPERTIES, values);

        //add side ID - or use default mandatory property
        values.put(column, parseType("???"));//todo get type from node
        //add side TYPE  - or use default mandatory property
        values.put(TYPE, parseType("string"));
        attributes.forEach(r -> values.put(r.getColumnName(), parseType(r.getDataType())));
        return sideProperties;
    }

    private void populateEdgeProperties(TraversalHop node, Map<String, Object> properties) {
        //generate attributes - including the origin table name (redundant fields)
        node.getAttributes().forEach(attribute -> properties.put(String.format("%s.%s", attribute.getTableName(), attribute.getColumnName()),
                Collections.singletonMap("type", parseType(attribute.getDataType()))));
    }

    private void populateProperties(ImplementationNode node, Map<String, Object> properties) {
        //generate field id
        node.getId().forEach(id -> properties.put(id.getColumnName(), Collections.singletonMap("type", parseType(id.getDatatype()))));
        //generate attributes
        node.getAttributes().forEach(attribute -> properties.put(attribute.getColumnName(), Collections.singletonMap("type", parseType(attribute.getDataType()))));
    }

    /**
     * generate specific entity type mapping
     *
     * @return
     */
    public Map<String, Object> generateEntityMapping(ImplementationNode implementationNode) {
        Map<String, Object> jsonMap = new HashMap<>();
        //populate index fields
        jsonMap.put(implementationNode.getTableName(), populateMappingIndexFields(implementationNode));

        return jsonMap;
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
            //default
            map.put("type", "text");
            map.put("fields", singletonMap("keyword", singletonMap("type", "keyword")));
        }
        return map;
    }

    /**
     * Load the shards & replication params from configuration
     *
     * @return
     */
    private Settings.Builder getSettings() {
        if (this.settingConfig.isEmpty()) {
            return Settings.builder()
                    .put("index.mapping.ignore_malformed", true)
//                    .put("index.mapping.single_type", true) //FIX - add this in E/S advanced version since 7.8
                    .put("index.number_of_shards", 3)
                    .put("index.number_of_replicas", 1);
        } else {
            Settings.Builder builder = Settings.builder();
            this.settingConfig.entrySet().forEach(
                    entry -> builder.put(entry.getKey(), entry.getValue().toString())
            );
            return builder;

        }
    }
}
