package com.fuse.domain.knowledge.datagen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuse.domain.knowledge.datagen.model.KnowledgeEntityBase;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.InvalidObjectException;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/22/2018.
 */
public class ElasticWriter {
    //region Constructors
    public ElasticWriter(Client client, ElasticConfiguration elasticConfiguration, int bulkSize) {
        this.client = client;
        this.elasticConfiguration = elasticConfiguration;
        this.bulkSize = bulkSize;
        this.mapper = new ObjectMapper();
    }
    //endregion

    //region Public Methods
    public void write(Iterable<ElasticDocument<KnowledgeEntityBase>> knowledgeDocuments) throws JsonProcessingException {
        BulkRequestBuilder bulkRequestBuilder = this.client.prepareBulk();
        int bulk = 0;
        for(ElasticDocument<KnowledgeEntityBase> knowledgeDocument : knowledgeDocuments) {
            bulkRequestBuilder.add(new IndexRequest(knowledgeDocument.getIndex(), knowledgeDocument.getType(), knowledgeDocument.getId())
                    .routing(knowledgeDocument.getRouting())
                    .source(this.mapper.writeValueAsBytes(knowledgeDocument.getSource()), XContentType.JSON));

            bulk++;

            if (bulk >= bulkSize) {
                BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
                if (bulkResponse.hasFailures()) {
                    int x = 5;
                }

                bulkRequestBuilder = client.prepareBulk();
                bulk = 0;
            }
        }

        if (bulk > 0) {
            BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                int x = 5;
            }
        }
    }
    //endregion

    //region Fields
    private Client client;
    private ElasticConfiguration elasticConfiguration;
    private int bulkSize;
    private ObjectMapper mapper;
    //endregion
}
