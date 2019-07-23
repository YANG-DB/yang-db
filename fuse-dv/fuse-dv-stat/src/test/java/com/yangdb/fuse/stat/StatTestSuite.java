package com.yangdb.fuse.stat;

import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.nio.file.Paths;

/**
 * Created by Roman on 20/06/2017.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        DemoStatTest.class,
        EsUtilTest.class,
        StatCalculatorDynamicFieldTest.class,
        StatCalculatorTest.class,
        StatConfigurationTest.class
})
public class StatTestSuite {
    private static final String CONFIGURATION_FILE_PATH = "statistics.test.properties";

    public static final String MAPPING_DATA_FILE_DRAGON_PATH = Paths.get("src", "test", "resources", "elastic.test.data.dragon.mapping.json").toString();
    public static final String MAPPING_DATA_FILE_FIRE_PATH = Paths.get("src", "test", "resources", "elastic.test.data.fire.mapping.json").toString();
    public static final String MAPPING_STAT_FILE_PATH = Paths.get("src", "test", "resources", "elastic.test.stat.mapping.json").toString();
    public static final String STAT_INDEX_NAME = "stat";

    public static TransportClient dataClient;
    public static TransportClient statClient;

    private static ElasticEmbeddedNode elasticEmbeddedNode;

    @BeforeClass
    public static void setup() throws Exception {
        elasticEmbeddedNode = GlobalElasticEmbeddedNode.getInstance();

        dataClient = elasticEmbeddedNode.getClient();
        statClient = elasticEmbeddedNode.getClient();
    }

    @AfterClass
    public static void tearDown() throws Exception {
    }
}
