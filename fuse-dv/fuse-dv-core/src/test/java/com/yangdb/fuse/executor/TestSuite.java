package com.yangdb.fuse.executor;

import com.yangdb.fuse.executor.elasticsearch.ElasticIndexProviderMappingFactoryTests;
import com.yangdb.fuse.executor.ontology.schema.IndexProviderBasedGraphLoaderTest;
import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import org.elasticsearch.client.Client;
import org.jooby.Jooby;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Roman on 21/06/2017.
 */
//todo "add TestSuite for both [IndexProviderBasedGraphLoaderTest,ElasticIndexProviderMappingFactoryTests]"
@Ignore("add TestSuite for both [IndexProviderBasedGraphLoaderTest,ElasticIndexProviderMappingFactoryTests]")
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ElasticIndexProviderMappingFactoryTests.class,
        IndexProviderBasedGraphLoaderTest.class
})
public class TestSuite {

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

    //region Fields
    private static Jooby app;
    //endregion
}
