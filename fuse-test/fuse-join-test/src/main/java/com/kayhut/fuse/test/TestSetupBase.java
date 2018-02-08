package com.kayhut.fuse.test;

import com.kayhut.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.services.FuseApp;
import com.kayhut.fuse.stat.StatCalculator;
import com.kayhut.fuse.stat.configuration.StatConfiguration;
import com.kayhut.fuse.test.util.FuseClient;
import com.kayhut.test.framework.index.*;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.transport.TransportClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.FIRE;

public abstract class TestSetupBase {


    protected ElasticEmbeddedNode instance;
    protected FuseApp fuseApp;
    protected FuseClient fuseClient;



    public void init() throws Exception {
        startFuse();
        loadData(instance.getClient());
    }

    public void cleanup(){
        cleanData(instance.getClient());
        teardownFuse();
    }

    private void teardownFuse() {
        if (fuseApp != null) {
            fuseApp.stop();
        }
    }

    private void startFuse() throws Exception {
        instance = GlobalElasticEmbeddedNode.getInstance();

        fuseApp = new FuseApp(new DefaultAppUrlSupplier("/fuse"))
                .conf(new File(Paths.get("fuse-test","fuse-join-test","src", "main", "resources", "conf", "application.engine2.dev.M2.discrete.conf").toString()), "m2.smartEpb");

        fuseApp.start("server.join=false");
        fuseClient = new FuseClient("http://localhost:8888/fuse");
    }

    protected abstract void loadData(TransportClient client) throws Exception;
    protected abstract void cleanData(TransportClient client);
}
