package com.kayhut.test.framework.populator;

import com.kayhut.test.framework.providers.GenericDataProvider;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;

import java.io.IOException;
import java.util.HashMap;


/**
 * Created by moti on 3/12/2017.
 */
public class ElasticDataPopulator implements DataPopulator {
    private TransportClient client;
    private String indexName;
    private String docType;
    private String idField;
    private GenericDataProvider provider;

    public ElasticDataPopulator(TransportClient client, String indexName, String docType, String idField, GenericDataProvider provider) {
        this.client = client;
        this.indexName = indexName;
        this.docType = docType;
        this.idField = idField;
        this.provider = provider;
    }

    private void indexDocument(HashMap<String, Object> doc) {
        IndexRequestBuilder indexRequestBuilder = client.prepareIndex()
                .setIndex(this.indexName)
                .setType(this.docType)
                .setOpType(IndexRequest.OpType.INDEX);
        if(doc.containsKey(idField))
            indexRequestBuilder = indexRequestBuilder.setId((String)doc.remove(idField));
        indexRequestBuilder = indexRequestBuilder.setSource(doc);
        IndexResponse indexResponse = indexRequestBuilder.execute()
                .actionGet();
        if(indexResponse.getShardInfo().getFailures().length != 0){
            throw new IllegalArgumentException("Inserting doc failed, doc = " + doc);
        }
    }

    @Override
    public void populate() throws IOException {
        this.provider.getDocuments().forEach(this::indexDocument);
    }
}
