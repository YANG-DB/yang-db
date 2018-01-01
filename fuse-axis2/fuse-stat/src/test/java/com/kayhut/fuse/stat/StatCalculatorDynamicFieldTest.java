package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.configuration.StatConfiguration;
import com.kayhut.fuse.stat.model.histogram.Histogram;
import com.kayhut.fuse.stat.model.histogram.HistogramDynamic;
import com.kayhut.fuse.stat.util.StatTestUtil;
import com.kayhut.test.framework.index.MappingFileElasticConfigurer;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static com.kayhut.fuse.stat.StatTestSuite.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by benishue on 08-Jun-17.
 */
public class StatCalculatorDynamicFieldTest {
    private static final int NUM_OF_DRAGONS_IN_INDEX_1 = 1000;
    private static final int NUM_OF_DRAGONS_IN_INDEX_2 = 555;
    private static final int NUM_OF_DRAGONS_IN_INDEX_3 = 200;
    private static final int NUM_OF_DRAGONS_IN_INDEX_4 = 100;

    private static final String STAT_INDEX_NAME = "stat";
    private static final String STAT_TYPE_NUMERIC_NAME = "bucketNumeric";

    private static final String DATA_INDEX_NAME_1 = "index1";
    private static final String DATA_INDEX_NAME_2 = "index2";
    private static final String DATA_INDEX_NAME_3 = "index3";
    private static final String DATA_INDEX_NAME_4 = "index4";

    private static final String DATA_TYPE_DRAGON = "dragon";
    private static final String DATA_TYPE_FIRE = "fire";

    private static final String DATA_FIELD_NAME_TIMESTAMP = "timestamp";

    private static final int DRAGON_MIN_AGE = 0;
    private static final int DRAGON_MAX_AGE = 100;
    private static final int DRAGON_ADDRESS_LENGTH = 20;
    private static final int DRAGON_NAME_PREFIX_LENGTH = 10;
    private static final int DRAGON_MIN_TEMP = 25;
    private static final int DRAGON_MAX_TEMP = 1000;
    private static final long DRAGON_START_DATE = 946684800000L;
    private static final long DRAGON_END_DATE = 978307200000L;
    private static final List<String> DRAGON_COLORS =
            Arrays.asList("red", "green", "yellow", "blue", "00", "11", "22", "33", "44", "55");
    private static final List<String> DRAGON_GENDERS =
            Arrays.asList("male", "female");

    private static final String CONFIGURATION_FILE_PATH = "statistics.test.dynamic.properties";

    @Test
    public void statDynamicTest() throws Exception {
        String jsonRelativePath = "src/test/resources/stats_fields_test_with_dynamics.json";
        Optional<Histogram> histogram = StatTestUtil.getHistogram(jsonRelativePath, DATA_TYPE_FIRE, DATA_FIELD_NAME_TIMESTAMP);
        assertTrue(histogram.isPresent());
        int numOfBins = ((HistogramDynamic) histogram.get()).getNumOfBins();

        StatCalculator.run(dataClient, statClient, new StatConfiguration(CONFIGURATION_FILE_PATH).getInstance());
        statClient.admin().indices().refresh(new RefreshRequest(STAT_INDEX_NAME)).actionGet();

        Set<Map<String, Object>> docs = StatTestUtil.searchByTerm(
                statClient,
                new String[]{STAT_INDEX_NAME},
                new String[]{STAT_TYPE_NUMERIC_NAME},
                "field", DATA_FIELD_NAME_TIMESTAMP);

        assertEquals(numOfBins, docs.size());
    }

    @BeforeClass
    public static void setup() throws Exception {
        new MappingFileElasticConfigurer(DATA_INDEX_NAME_1, MAPPING_DATA_FILE_DRAGON_PATH).configure(dataClient);
        new ElasticDataPopulator(
                dataClient,
                DATA_INDEX_NAME_1,
                "pge",
                "id",
                () -> StatTestUtil.createDragons(NUM_OF_DRAGONS_IN_INDEX_1,
                        DRAGON_MIN_AGE,
                        DRAGON_MAX_AGE,
                        DRAGON_NAME_PREFIX_LENGTH,
                        DRAGON_COLORS,
                        DRAGON_GENDERS,
                        DRAGON_ADDRESS_LENGTH)).populate();

        new MappingFileElasticConfigurer(DATA_INDEX_NAME_2, MAPPING_DATA_FILE_DRAGON_PATH).configure(dataClient);
        new ElasticDataPopulator(
                dataClient,
                DATA_INDEX_NAME_2,
                "pge",
                "id",
                () -> StatTestUtil.createDragons(NUM_OF_DRAGONS_IN_INDEX_2,
                        DRAGON_MIN_AGE,
                        DRAGON_MAX_AGE,
                        DRAGON_NAME_PREFIX_LENGTH,
                        DRAGON_COLORS,
                        DRAGON_GENDERS,
                        DRAGON_ADDRESS_LENGTH)).populate();

        new MappingFileElasticConfigurer(DATA_INDEX_NAME_3, MAPPING_DATA_FILE_FIRE_PATH).configure(dataClient);
        new ElasticDataPopulator(
                dataClient,
                DATA_INDEX_NAME_3,
                "pge",
                "id",
                () -> StatTestUtil.createDragonFireDragonEdges(
                        NUM_OF_DRAGONS_IN_INDEX_3,
                        DRAGON_START_DATE,
                        DRAGON_END_DATE,
                        DRAGON_MIN_TEMP,
                        DRAGON_MAX_TEMP
                )).populate();

        new MappingFileElasticConfigurer(DATA_INDEX_NAME_4, MAPPING_DATA_FILE_FIRE_PATH).configure(dataClient);
        new ElasticDataPopulator(
                dataClient,
                DATA_INDEX_NAME_4,
                "pge",
                "id",
                () -> StatTestUtil.createDragonFireDragonEdges(
                        NUM_OF_DRAGONS_IN_INDEX_4,
                        DRAGON_START_DATE,
                        DRAGON_END_DATE,
                        DRAGON_MIN_TEMP,
                        DRAGON_MAX_TEMP
                )).populate();

        if (statClient != null) {
            new MappingFileElasticConfigurer(STAT_INDEX_NAME, MAPPING_STAT_FILE_PATH).configure(statClient);
        }

        dataClient.admin().indices().refresh(new RefreshRequest(
                DATA_INDEX_NAME_1, DATA_INDEX_NAME_2, DATA_INDEX_NAME_3, DATA_INDEX_NAME_4))
                .actionGet();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (statClient != null) {
            statClient.admin().indices().delete(new DeleteIndexRequest(STAT_INDEX_NAME)).actionGet();
        }

        if (dataClient != null) {
            dataClient.admin().indices().delete(new DeleteIndexRequest(
                    DATA_INDEX_NAME_1,
                    DATA_INDEX_NAME_2,
                    DATA_INDEX_NAME_3,
                    DATA_INDEX_NAME_4
            )).actionGet();
        }
    }
}
