package com.kayhut.fuse.stat.es.populator;

import com.kayhut.fuse.stat.es.providers.GenericDataProvider;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;

import java.io.IOException;
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
    private static final int BULK_SIZE = 500;

    public ElasticDataPopulator(TransportClient client, String indexName, String docType, String idField, GenericDataProvider provider) {
        this.client = client;
        this.indexName = indexName;
        this.docType = docType;
        this.idField = idField;
        this.provider = provider;
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
        int currentBulkSize = 0;

        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        for (Map<String, Object> document : this.provider.getDocuments()) {
            currentBulkSize++;
            IndexRequestBuilder indexRequestBuilder = documentIndexRequest(document);
            bulkRequestBuilder.add(indexRequestBuilder);
            if (currentBulkSize == BULK_SIZE) {
                BulkResponse bulkItemResponses = bulkRequestBuilder.execute().actionGet();
                if (bulkItemResponses.hasFailures()) {
                    throw new IllegalArgumentException(bulkItemResponses.buildFailureMessage());
                }
                bulkRequestBuilder = client.prepareBulk();
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
