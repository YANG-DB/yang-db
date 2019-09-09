package com.yangdb.fuse.executor.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.RelationshipType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.model.schema.Relation;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.Tuple2;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;

import javax.management.relation.RelationType;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Collections.singletonMap;

public class IndexProviderMappingFactory {

    private ObjectMapper mapper = new ObjectMapper();

    private Client client;
    private RawSchema schema;
    private IndexProvider indexProvider;
    private Ontology.Accessor ontology;

    @Inject
    public IndexProviderMappingFactory(Client client, RawSchema schema, Ontology ontology, IndexProvider indexProvider) {
        this.client = client;
        this.schema = schema;
        this.indexProvider = indexProvider;
        this.ontology = new Ontology.Accessor(ontology);
    }

    public List<Tuple2<String, Boolean>> generateMappings() {
        List<Tuple2<String,AcknowledgedResponse>> responses = new ArrayList<>();
        mapEntities(responses);
        mapRelations(responses);
        return responses.stream().map(r->new Tuple2<>(r._1,r._2.isAcknowledged())).collect(Collectors.toList());
    }

    private void mapRelations(List<Tuple2<String, AcknowledgedResponse>> responses) {
        StreamSupport.stream(ontology.relations().spliterator(), false).forEach(r -> {
            String mapping = indexProvider.getRelation(r.getName()).orElseThrow(
                    () -> new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No entity with name " + r + " found in ontology")))
                    .getPartition();

            switch (mapping) {
                case "static":
                    //static index
                    indexProvider.getRelation(r.getName()).get().getProps().getValues().forEach(v -> {
                        PutIndexTemplateRequest request = new PutIndexTemplateRequest(v.toLowerCase());
                        request.patterns(Arrays.asList(r.getName(), String.format("%s%s", v, "*")))
                                .settings(generateSettings(r,v))
                                .mapping(v,generateMapping(r,v));
                        //add response to list of responses
                        responses.add(new Tuple2<>(v,client.admin().indices().putTemplate(request).actionGet()));
                    });
                    break;
                case "time":
                    //todo implement time partition
                    break;
            }
        });
    }

    private void mapEntities(List<Tuple2<String, AcknowledgedResponse>> responses) {
        StreamSupport.stream(ontology.entities().spliterator(), false).forEach(e -> {
            String mapping = indexProvider.getEntity(e.getName()).orElseThrow(
                    () -> new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No entity with name " + e + " found in ontology")))
                    .getPartition();

            switch (mapping) {
                case "static":
                    //static index
                    indexProvider.getEntity(e.getName()).get().getProps().getValues().forEach(v -> {
                        PutIndexTemplateRequest request = new PutIndexTemplateRequest(v.toLowerCase());
                        request.patterns(Arrays.asList(e.getName(), String.format("%s%s", v, "*")))
                                .settings(generateSettings(e,v))
                                .mapping(v,generateMapping(e,v));
                        //add response to list of responses
                        responses.add(new Tuple2<>(v,client.admin().indices().putTemplate(request).actionGet()));
                    });
                    break;
                case "time":
                    //todo implement time partition
                    break;
            }
        });
    }

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

    public Map<String, Object> generateMapping(RelationshipType relationshipType, String label) {
        Optional<RelationshipType> relation = ontology.relation(relationshipType.getName());
        if (!relation.isPresent())
            throw new FuseError.FuseErrorException(new FuseError("Mapping generation exception", "No relation    with name " + label + " found in ontology"));

        Map<String, Object> jsonMap = new HashMap<>();

        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        //populate fields & metadata
        relation.get().getMetadata().forEach(v -> properties.put(v, parseType(ontology.property$(v).getType())));
        relation.get().getProperties().forEach(v -> properties.put(v, parseType(ontology.property$(v).getType())));
        jsonMap.put(label, mapping);

        return jsonMap;
    }

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

    public Settings generateSettings(EntityType entityType,String label) {

        if(!ontology.entity(entityType.getName()).get().getMetadata().contains("id"))
            throw new FuseError.FuseErrorException(new FuseError("Schema generation exception"," Entity "+ label+" not containing id metadata property "));

        return builder();
    }

    public Settings generateSettings(RelationshipType relationType, String label) {

        if(!ontology.relation(relationType.getName()).get().getMetadata().contains("id"))
            throw new FuseError.FuseErrorException(new FuseError("Schema generation exception"," Relationship "+ label+" not containing id metadata property "));

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
