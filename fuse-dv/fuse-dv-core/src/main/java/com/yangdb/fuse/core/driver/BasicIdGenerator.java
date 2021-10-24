package com.yangdb.fuse.core.driver;

/*-
 * #%L
 * fuse-dv-core
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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.model.Range;
import org.opensearch.OpenSearchParseException;
import org.opensearch.action.admin.indices.create.CreateIndexRequest;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.Client;
import org.opensearch.index.IndexNotFoundException;
import org.opensearch.index.engine.VersionConflictEngineException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yangdb.fuse.executor.ExecutorModule.globalClient;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public class BasicIdGenerator implements IdGeneratorDriver<Range> {
    public static final String indexNameParameter = "BasicIdGenerator.@indexName";
    public static final String IDSEQUENCE = "idsequence";

    //region Constructors
    @Inject
    public BasicIdGenerator(
            @Named(globalClient) Client client,
            @Named(indexNameParameter) String indexName) {
        this.client = client;
        this.sync = new Object();

        this.indexName = indexName;
    }
    //endregion

    //region IdGenerator Implementation
    @Override
    public Range getNext(String genName, int numIds) {
        synchronized (this.sync) {
            while (true) {
                try {
                    GetResponse getResponse = this.client.get(new GetRequest(this.indexName, IDSEQUENCE, genName)).actionGet();
                    long currentId = 1l;
                    if (getResponse.isExists()) {
                        currentId = ((Number) getResponse.getSource().get("value")).longValue();
                    } else {
                        addFirstSequenceId(genName);
                    }
                    Map<String, Object> newValue = new HashMap<>(1);
                    newValue.put("value", currentId + numIds);

                    try {
                        IndexResponse indexResponse = this.client.index(new IndexRequest(
                                getResponse.getIndex(),
                                getResponse.getType(),
                                getResponse.getId()).version(getResponse.getVersion())
                                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                                .source(newValue)).actionGet();

                        if (indexResponse.status().getStatus() == 200) {
                            return new Range(currentId, currentId + numIds);
                        }
                    } catch (VersionConflictEngineException ex) {
                        //retry
                    }
                } catch (IndexNotFoundException ex) {
                    //retry
                    generateIndex();
                    addFirstSequenceId(genName);
                }
            }
        }
    }

    @Override
    public boolean init(List<String> names) {
        try {
            generateIndex();
        } catch (OpenSearchParseException error){
            //index already exists
        }
        names.forEach(this::addFirstSequenceId);
        return true;
    }

    private void addFirstSequenceId(String genName) {
        this.client.index(new IndexRequest(this.indexName).id(genName).type(IDSEQUENCE).source(Collections.singletonMap("value", 1l))).actionGet();
    }

    private void generateIndex() {
        this.client.admin().indices()
                .create(new CreateIndexRequest()
                        .index(this.indexName)).actionGet();
    }

    //endregion

    //region Fields
    private Client client;
    private Object sync;
    private String indexName;
    //endregion
}
