package com.yangdb.fuse.executor.sql;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.mapping.IndexMapping;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.ElasticsearchRequest;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.response.ElasticsearchResponse;
import com.yangdb.fuse.executor.elasticsearch.ClientProvider;
import com.yangdb.fuse.executor.elasticsearch.ElasticIndexProviderMappingFactory;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.Entity;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import javaslang.Tuple2;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.threadpool.ThreadPool;

import javax.inject.Inject;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.amazon.opendistroforelasticsearch.sql.legacy.executor.AsyncRestExecutor.SEARCH_THREAD_POOL_NAME;
import static com.amazon.opendistroforelasticsearch.sql.legacy.utils.LogUtils.withCurrentContext;
import static com.yangdb.fuse.executor.elasticsearch.ElasticIndexProviderMappingFactory.PROPERTIES;

public class ElasticsearchFuseClient implements ElasticsearchClient {
    private Client client;
    private Ontology.Accessor provider;
    private RawSchema schema;
    private IndexProvider indexProvider;
    private ElasticIndexProviderMappingFactory mappingFactory;

    @Inject
    public ElasticsearchFuseClient(Client client, Ontology ontology, RawSchema schema, IndexProvider indexProvider, ElasticIndexProviderMappingFactory mappingFactory) {
        this.client = client;
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
        return request.search(
                req -> client.search(req).actionGet(),
                req -> client.searchScroll(req).actionGet()
        );
    }

    @Override
    public void cleanup(ElasticsearchRequest request) {
        request.clean(scrollId -> client.prepareClearScroll().addScrollId(scrollId).get());

    }

    @Override
    public void schedule(Runnable task) {
        ThreadPool threadPool = client.threadPool();
        threadPool.schedule(
                withCurrentContext(task),
                new TimeValue(0),
                SEARCH_THREAD_POOL_NAME
        );

    }
}
