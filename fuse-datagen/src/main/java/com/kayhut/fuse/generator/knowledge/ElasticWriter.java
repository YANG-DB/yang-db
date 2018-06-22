package com.kayhut.fuse.generator.knowledge;

import com.kayhut.fuse.generator.knowledge.model.Entity;
import com.kayhut.fuse.generator.knowledge.model.EvalueInt;
import com.kayhut.fuse.generator.knowledge.model.EvalueString;
import com.kayhut.fuse.generator.knowledge.model.KnowledgeEntityBase;
import javaslang.collection.Stream;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;

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
    }
    //endregion

    //region Public Methods
    public void write(Iterable<ElasticDocument<KnowledgeEntityBase>> knowledgeDocuments) {
        BulkRequestBuilder bulkRequestBuilder = this.client.prepareBulk();
        int bulk = 0;
        for(ElasticDocument<KnowledgeEntityBase> knowledgeDocument : knowledgeDocuments) {
            bulkRequestBuilder.add(new IndexRequest(knowledgeDocument.getIndex(), knowledgeDocument.getType(), knowledgeDocument.getId())
                    .routing(knowledgeDocument.getRouting())
                    .source(knowledgeDocument.getSource()));

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
    //endregion
}
