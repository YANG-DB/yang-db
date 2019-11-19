package com.yangdb.fuse.executor;

import com.yangdb.fuse.executor.elasticsearch.ElasticIndexProviderMappingFactoryIT;
import com.yangdb.fuse.executor.ontology.schema.IndexProviderBasedCSVLoaderIT;
import com.yangdb.fuse.executor.ontology.schema.IndexProviderBasedGraphLoaderIT;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import com.yangdb.test.BaseSuiteMarker;
import org.elasticsearch.client.Client;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Roman on 21/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        IndexProviderBasedGraphLoaderIT.class,
        IndexProviderBasedCSVLoaderIT.class,
        ElasticIndexProviderMappingFactoryIT.class
})
public class TestSuiteIndexProviderSuite implements BaseSuiteMarker {

    private static ElasticEmbeddedNode elasticEmbeddedNode;
    private static Client client;

    @BeforeClass
    public static void setup() throws Exception {
        init(true);
    }

    private static void init(boolean embedded) throws Exception {
        // Start embedded ES
        if(embedded) {
            elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance();
            client = elasticEmbeddedNode.getClient();
        } else {
            //use existing running ES
            client = elasticEmbeddedNode.getClient();
        }

    }

    @AfterClass
    public static void tearDown() throws Exception {
        if(elasticEmbeddedNode!=null)
            elasticEmbeddedNode.close();

    }

    public static Client getClient() {
        return client;
    }

    //region Fields
    private static Jooby app;
    //endregion
}
