package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.*;
import com.kayhut.fuse.stat.configuration.StatConfiguration;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.fuse.stat.util.StatTestUtil;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.index.ElasticIndexConfigurer;
import com.kayhut.test.framework.index.MappingFileElasticConfigurer;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

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

    private static final String MAPPING_DATA_FILE_DRAGON_PATH = Paths.get("src", "test", "resources", "elastic.test.data.dragon.mapping.json").toString();
    private static final String MAPPING_DATA_FILE_FIRE_PATH = Paths.get("src", "test", "resources", "elastic.test.data.fire.mapping.json").toString();
    private static final String MAPPING_STAT_FILE_PATH = Paths.get("src", "test", "resources", "elastic.test.stat.mapping.json").toString();

    private static final String DATA_INDEX_NAME_1 = "index1";
    private static final String DATA_INDEX_NAME_2 = "index2";
    private static final String DATA_INDEX_NAME_3 = "index3";
    private static final String DATA_INDEX_NAME_4 = "index4";

    private static final String STAT_INDEX_NAME = "stat";

    public static TransportClient dataClient;
    public static TransportClient statClient;

    private static ElasticEmbeddedNode elasticEmbeddedNode;

    @BeforeClass
    public static void setup() throws Exception {
        Configuration configuration = new StatConfiguration(CONFIGURATION_FILE_PATH).getInstance();

        dataClient = ClientProvider.getDataClient(configuration);
        statClient = ClientProvider.getDataClient(configuration);

        MappingFileElasticConfigurer configurerIndex1 = new MappingFileElasticConfigurer(DATA_INDEX_NAME_1, MAPPING_DATA_FILE_DRAGON_PATH);
        MappingFileElasticConfigurer configurerIndex2 = new MappingFileElasticConfigurer(DATA_INDEX_NAME_2, MAPPING_DATA_FILE_DRAGON_PATH);
        MappingFileElasticConfigurer configurerIndex3 = new MappingFileElasticConfigurer(DATA_INDEX_NAME_3, MAPPING_DATA_FILE_FIRE_PATH);
        MappingFileElasticConfigurer configurerIndex4 = new MappingFileElasticConfigurer(DATA_INDEX_NAME_4, MAPPING_DATA_FILE_FIRE_PATH);


        MappingFileElasticConfigurer configurerStat = new MappingFileElasticConfigurer(STAT_INDEX_NAME, MAPPING_STAT_FILE_PATH);

        elasticEmbeddedNode = new ElasticEmbeddedNode(
                configurerIndex1,
                configurerIndex2,
                configurerIndex3,
                configurerIndex4,
                configurerStat);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (statClient != null) {
            statClient.close();
            statClient = null;
        }

        if (dataClient != null) {
            dataClient.close();
            dataClient = null;
        }

        elasticEmbeddedNode.close();
    }
}
