package com.yangdb.fuse.sql;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.mapping.IndexMapping;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.ElasticsearchRequest;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.response.ElasticsearchResponse;
import com.yangdb.fuse.executor.elasticsearch.ElasticIndexProviderMappingFactory;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.Entity;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.Tuple2;

import javax.inject.Inject;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.yangdb.fuse.executor.elasticsearch.ElasticIndexProviderMappingFactory.PROPERTIES;

public class ElasticsearchFuseClient implements ElasticsearchClient {
    private Ontology.Accessor provider;
    private RawSchema schema;
    private IndexProvider indexProvider;
    private ElasticIndexProviderMappingFactory mappingFactory;

    @Inject
    public ElasticsearchFuseClient(Ontology ontology, RawSchema schema, IndexProvider indexProvider, ElasticIndexProviderMappingFactory mappingFactory) {
        this.provider = new Ontology.Accessor(ontology);
        this.schema = schema;
        this.indexProvider = indexProvider;
        this.mappingFactory = mappingFactory;
    }

    @Override
    /**
     * search the appropriate index according to the ontology entities
     * create an IndexMapping object according to the actual mapped index provider
     */
    public Map<String, IndexMapping> getIndexMappings(String indexExpression) {
        //get ontology entity
        EntityType entityType = provider.entity(indexExpression)
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No entity exists with the following indexExpression " + indexExpression, "IndexExpression not found in ontology")));
        //get schematic entity
        Entity entity = indexProvider.getEntity(entityType.getName())
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No index provider exists with the following indexExpression " + indexExpression, "IndexExpression not found in index provider")));
        //get index partition(s)
        IndexPartitions partition = schema.getPartition(entityType.getName());

        //collect all fields according to mapping factory definitions as they effected the actual schema structure
        Map<String, Object> fields = (Map<String, Object>) mappingFactory.populateMappingIndexFields(entity, entityType).get(PROPERTIES);
        Map<String, String> fieldsMap = fields.entrySet().stream()
                .filter(entry -> Map.class.isAssignableFrom(entry.getValue().getClass()))
                .filter(entry -> ((Map) entry.getValue()).containsKey("type"))
                .map(entry -> new Tuple2<>(entry.getKey(), ((Map) entry.getValue()).get("type").toString()))
                .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));

        //collect the partition indices to a map with the fieldsMap as the IndexMapping value
        return StreamSupport.stream(partition.getIndices().spliterator(), true)
                .collect(Collectors.toMap(Function.identity(), s -> new IndexMapping(fieldsMap)));

    }

    @Override
    public ElasticsearchResponse search(ElasticsearchRequest request) {
        return null;
    }

    @Override
    public void cleanup(ElasticsearchRequest request) {

    }

    @Override
    public void schedule(Runnable task) {

    }
}
