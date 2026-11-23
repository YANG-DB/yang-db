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
import org.opensearch.ResourceAlreadyExistsException;
import org.opensearch.action.DocWriteRequest;
import org.opensearch.action.admin.indices.create.CreateIndexRequest;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.support.WriteRequest;
import org.opensearch.client.Client;
import org.opensearch.index.IndexNotFoundException;
import org.opensearch.index.engine.VersionConflictEngineException;

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
    private static final int MAX_CONFLICT_ATTEMPTS = 100;

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

    /**
     * read the counter, then write counter + numIds only if no one else wrote it since the read (optimistic concurrency
     * on the document's seq_no / primary_term). A conflict means another generator (e.g. another service node) took
     * that range: read again. The counter document's id is the generator name.
     */
    private Range getNextInternal(String genName, int numIds) {
        synchronized (this.sync) {
            try {
                for (int attempt = 0; attempt < MAX_CONFLICT_ATTEMPTS; attempt++) {
                    GetResponse getResponse = this.client.get(new GetRequest(this.indexName, genName)).actionGet();
                    if (!getResponse.isExists()) {
                        addFirstSequenceId(genName);
                        continue;
                    }
                    long currentId = ((Number) getResponse.getSource().get("value")).longValue();
                    Map<String, Object> newValue = new HashMap<>(1);
                    newValue.put("value", currentId + numIds);

                    try {
                        this.client.index(new IndexRequest(this.indexName)
                                .id(genName)
                                .setIfSeqNo(getResponse.getSeqNo())
                                .setIfPrimaryTerm(getResponse.getPrimaryTerm())
                                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                                .source(newValue)).actionGet();
                        return new Range(currentId, currentId + numIds);
                    } catch (VersionConflictEngineException ex) {
                        //another generator took this range - read the counter again
                    }
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
        generateIndex();
        names.forEach(this::addFirstSequenceId);
        return true;
    }

    /**
     * create the counter at 1 unless it exists: re-running init (or two nodes starting) must not reset a counter
     */
    private void addFirstSequenceId(String genName) {
        try {
            this.client.index(new IndexRequest(this.indexName).id(genName)
                    .opType(DocWriteRequest.OpType.CREATE)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                    .source(Collections.singletonMap("value", 1l))).actionGet();
        } catch (VersionConflictEngineException ex) {
            //counter already exists
        }
    }

    private void generateIndex() {
        try {
            this.client.admin().indices()
                    .create(new CreateIndexRequest()
                            .index(this.indexName)).actionGet();
        } catch (ResourceAlreadyExistsException ex) {
            //index already exists
        }
    }

    //endregion

    //region Fields
    private Client client;
    private Object sync;
    private String indexName;
    //endregion
}
