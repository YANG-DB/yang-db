package com.kayhut.test.framework.populator;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.kayhut.test.framework.providers.GenericDataProvider;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Created by moti on 3/12/2017.
 */
public class ElasticDataPopulator implements DataPopulator {
    private TransportClient client;
    private String indexName;
    private String docType;
    private String idField;
    private GenericDataProvider provider;
    private static int BULK_SIZE = 500;

    public ElasticDataPopulator(TransportClient client, String indexName, String docType, String idField, GenericDataProvider provider) {
        this.client = client;
        this.indexName = indexName;
        this.docType = docType;
        this.idField = idField;
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
                .setType(this.docType)
                .setOpType(IndexRequest.OpType.INDEX);
        if(doc.containsKey(idField))
            indexRequestBuilder = indexRequestBuilder.setId((String)doc.remove(idField));
        indexRequestBuilder = indexRequestBuilder.setSource(doc);
        return indexRequestBuilder;
    }

    @Override
    public void populate() throws IOException {
        int i = 0;

        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        for(Iterator<Map<String, Object>> iterator = this.provider.getDocuments().iterator(); iterator.hasNext();){
            Map<String, Object> document = iterator.next();
            i++;
            IndexRequestBuilder indexRequestBuilder = documentIndexRequest(document);
            bulkRequestBuilder.add(indexRequestBuilder);
            if(i % BULK_SIZE == 0){
                BulkResponse bulkItemResponses = bulkRequestBuilder.execute().actionGet();
                if(bulkItemResponses.hasFailures()){
                    throw new IllegalArgumentException(bulkItemResponses.buildFailureMessage());
                }
                bulkRequestBuilder = client.prepareBulk();
            }
        }
        BulkResponse bulkItemResponses = bulkRequestBuilder.execute().actionGet();
        if(bulkItemResponses.hasFailures()){
            throw new IllegalArgumentException(bulkItemResponses.buildFailureMessage());
        }

    }
}
