package com.yangdb.fuse.test.framework.populator;

/*-
 * #%L
 * fuse-test-framework
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

import com.yangdb.fuse.client.elastic.BaseFuseElasticClient;
import com.yangdb.fuse.test.framework.providers.GenericDataProvider;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Created by moti on 3/12/2017.
 */
public class ElasticDataPopulator implements DataPopulator {
    private BaseFuseElasticClient client;
    private String indexName;
    private String docType;
    private String idField;
    private boolean removeIdField;
    private String routingField;
    private boolean removeRoutingField;
    private GenericDataProvider provider;
    private static int BULK_SIZE = 10000;

    public ElasticDataPopulator(BaseFuseElasticClient client, String indexName, String docType, String idField, GenericDataProvider provider) {
        this(client, indexName, docType, idField, true, null, true, provider);
    }

    public ElasticDataPopulator(
            BaseFuseElasticClient client,
            String indexName,
            String docType,
            String idField,
            boolean removeIdField,
            String routingField,
            boolean removeRoutingField,
            GenericDataProvider provider) {
        this.client = client;
        this.indexName = indexName;
        this.docType = docType;
        this.idField = idField;
        this.removeIdField = removeIdField;
        this.routingField = routingField;
        this.removeRoutingField = removeRoutingField;
        this.provider = provider;
    }

    private void indexDocument(HashMap<String, Object> doc) {
        IndexRequestBuilder indexRequestBuilder = documentIndexRequest(doc);
        IndexResponse indexResponse = indexRequestBuilder.execute()
                .actionGet();
        if(indexResponse.getShardInfo().getFailures().length != 0){
            throw new IllegalArgumentException("Inserting doc failed, doc = " + doc);
        }
    }

    private IndexRequestBuilder documentIndexRequest(Map<String, Object> doc){
        IndexRequestBuilder indexRequestBuilder = client.prepareIndex()
                .setIndex(this.indexName)
                .setRouting(this.routingField != null ? (String)doc.get(this.routingField) : (String)doc.get(this.idField))
                .setType(this.docType)
                .setOpType(IndexRequest.OpType.INDEX);

        if(doc.containsKey(this.idField)) {
            indexRequestBuilder = indexRequestBuilder.setId((String)doc.get(this.idField));
            if (this.removeIdField) {
                doc.remove(this.idField);
            }
        }

        if (this.routingField != null && this.removeRoutingField) {
            doc.remove(this.routingField);
        }

        indexRequestBuilder = indexRequestBuilder.setSource(doc);
        return indexRequestBuilder;
    }

    @Override
    public void populate() throws Exception {
        int currentBulkSize = 0;

        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk().setRefreshPolicy(WriteRequest.RefreshPolicy.NONE);
        for(Iterator<Map<String, Object>> iterator = this.provider.getDocuments().iterator(); iterator.hasNext();){
            Map<String, Object> document = iterator.next();
            currentBulkSize++;
            IndexRequestBuilder indexRequestBuilder = documentIndexRequest(document);
            bulkRequestBuilder.add(indexRequestBuilder);
            if(currentBulkSize == BULK_SIZE){
                BulkResponse bulkItemResponses = bulkRequestBuilder.execute().actionGet();
                if(bulkItemResponses.hasFailures()){
                    throw new IllegalArgumentException(bulkItemResponses.buildFailureMessage());
                }
                bulkRequestBuilder = client.prepareBulk().setRefreshPolicy(WriteRequest.RefreshPolicy.NONE);
                currentBulkSize = 0;
            }
        }

        if (currentBulkSize > 0) {
            BulkResponse bulkItemResponses = bulkRequestBuilder.execute().actionGet();
            if (bulkItemResponses.hasFailures()) {
                throw new IllegalArgumentException(bulkItemResponses.buildFailureMessage());
            }
        }
    }
}
