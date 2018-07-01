package com.kayhut.fuse.test;

import com.kayhut.fuse.test.framework.populator.ElasticDataPopulator;
import org.elasticsearch.client.transport.TransportClient;

public abstract class TestSetupBase {
    protected ElasticEmbeddedNode instance;

    public void init() throws Exception {
        instance = GlobalElasticEmbeddedNode.getInstance();
        loadData(instance.getClient());
    }

    public void cleanup(){
        cleanData(instance.getClient());
    }


    protected abstract void loadData(TransportClient client) throws Exception;
    protected abstract void cleanData(TransportClient client);
}
