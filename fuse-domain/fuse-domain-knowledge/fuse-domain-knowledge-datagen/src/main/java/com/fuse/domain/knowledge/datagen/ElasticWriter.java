package com.fuse.domain.knowledge.datagen;

/*-
 *
 * fuse-domain-knowledge-datagen
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.fuse.domain.knowledge.datagen.model.KnowledgeEntityBase;
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
