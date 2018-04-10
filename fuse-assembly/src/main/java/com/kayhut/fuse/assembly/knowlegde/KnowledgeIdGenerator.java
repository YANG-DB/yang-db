package com.kayhut.fuse.assembly.knowlegde;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.driver.IdGeneratorDriver;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.engine.VersionConflictEngineException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public class KnowledgeIdGenerator implements IdGeneratorDriver<Object> {
    public static final String indexNameParameter = "KnowledgeIdGenerator.@indexName";

    //region Constructors
    @Inject
    public KnowledgeIdGenerator(
            Client client,
            @Named(indexNameParameter) String indexName) {
        this.client = client;
        this.sync = new Object();

        this.indexName = indexName;
    }
    //endregion

    //region IdGenerator Implementation
    @Override
    public Object getNext(String genName, int numIds) {
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
                        return new KnowledgeIdGenerator.Range(currentId, currentId + numIds);
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

    //region Range
    public static class Range {
        //region Constructors
        public Range() {

        }

        public Range(long lower, long upper) {
            this.lower = lower;
            this.upper = upper;
        }
        //endregion

        //region Properties
        public long getUpper() {
            return upper;
        }

        public void setUpper(long upper) {
            this.upper = upper;
        }

        public long getLower() {
            return lower;
        }

        public void setLower(long lower) {
            this.lower = lower;
        }
        //endregion

        //region Fields
        private long upper;
        private long lower;
        //endregion
    }
    //endregion
}
