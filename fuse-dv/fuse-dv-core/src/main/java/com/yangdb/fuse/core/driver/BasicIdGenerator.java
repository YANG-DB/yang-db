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
import com.yangdb.fuse.model.resourceInfo.FuseError;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.engine.VersionConflictEngineException;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.yangdb.fuse.executor.ExecutorModule.globalClient;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public class BasicIdGenerator implements IdGeneratorDriver<Range> {
    public static final String indexNameParameter = "BasicIdGenerator.@indexName";
    public static final String IDSEQUENCE = "idsequence";

    //retry mechanism
    private RetryConfig config = RetryConfig.custom()
            .maxAttempts(5)
            .waitDuration(Duration.ofSeconds(2))
            .build();
    private RetryRegistry registry = RetryRegistry.of(config);
    private Retry retry = registry.retry("getNextInternal", config);

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
        //service supplier
        Supplier<Range> rangeSupplier =
                () -> getNextInternal(genName, numIds);
        // retry supplier
        Supplier<Range> rangeSupplierService =
                Retry.decorateSupplier(retry, rangeSupplier);
        //activate
        return rangeSupplierService.get();
    }

    private Range getNextInternal(String genName, int numIds) {
        synchronized (this.sync) {
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
                //before retry generate requeired index elements
                generateIndex();
                addFirstSequenceId(genName);
            } catch (Throwable t) {
                throw new FuseError.FuseErrorException("Error while attempting to get Snowflake ID ...", t);
            }
        }
        //activate retry
        throw new RuntimeException("Error while attempting to get Snowflake ID -> Will soon attempt retry ... ");
    }

    @Override
    public boolean init(List<String> names) {
        try {
            generateIndex();
        } catch (ElasticsearchException error) {
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
