package com.kayhut.fuse.assembly.knowledge;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import com.kayhut.fuse.model.Range;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.engine.VersionConflictEngineException;

import java.util.HashMap;
import java.util.Map;

import static com.kayhut.fuse.executor.ExecutorModule.globalClient;
import static com.kayhut.fuse.executor.elasticsearch.logging.LoggingClient.clientParameter;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public class KnowledgeIdGenerator implements IdGeneratorDriver<Range> {
    public static final String indexNameParameter = "KnowledgeIdGenerator.@indexName";

    //region Constructors
    @Inject
    public KnowledgeIdGenerator(
            @Named(globalClient)Client client,
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
            while(true) {
                GetResponse getResponse = this.client.get(new GetRequest(this.indexName, "idsequence", genName)).actionGet();
                long currentId = ((Number) getResponse.getSource().get("value")).longValue();
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
                    int x = 5;
                }
            }
        }
    }
    //endregion

    //region Fields
    private Client client;
    private Object sync;
    private String indexName;
    //endregion
}
