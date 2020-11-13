package com.yangdb.fuse;

import com.yangdb.test.BaseSuiteMarker;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({})
public class TestSuiteAPISuite implements BaseSuiteMarker {

    public static TestSetupService suite = new TestSetupService() {
        @Override
        protected void loadData(TransportClient client) {
            //todo ?
        }

        @Override
        protected void cleanData(TransportClient client) {
            //todo ?
        }
    };

    @BeforeClass
    public static void setup() throws Exception {
        suite.init();
    }



    @AfterClass
    public static void tearDown() throws Exception {
        suite.cleanup();
    }
}
