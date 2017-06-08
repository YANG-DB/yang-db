package com.kayhut.fuse.stat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.stat.configuration.StatConfiguration;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.fuse.stat.model.configuration.Field;
import com.kayhut.fuse.stat.model.configuration.StatContainer;
import com.kayhut.fuse.stat.model.configuration.Type;
import com.kayhut.fuse.stat.model.histogram.Histogram;
import com.kayhut.fuse.stat.model.histogram.HistogramDynamic;
import com.kayhut.fuse.stat.model.histogram.HistogramNumeric;
import com.kayhut.fuse.stat.util.EsUtil;
import com.kayhut.fuse.stat.util.StatTestUtil;
import com.kayhut.fuse.stat.util.StatUtil;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.index.ElasticIndexConfigurer;
import com.kayhut.test.framework.index.MappingFileElasticConfigurer;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsLookupQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.cypher.internal.frontend.v2_3.ast.Foreach;
import org.skyscreamer.jsonassert.JSONAssert;

import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by benishue on 08-Jun-17.
 */
public class statCalculatorDynamicFieldTest {


    private static TransportClient dataClient;
    private static TransportClient statClient;
    private static ElasticEmbeddedNode elasticEmbeddedNode;
    private static final String MAPPING_DATA_FILE_DRAGON_PATH = Paths.get("src", "test", "resources", "elastic.test.data.dragon.mapping.json").toString();
    private static final String MAPPING_DATA_FILE_FIRE_PATH = Paths.get("src", "test", "resources", "elastic.test.data.fire.mapping.json").toString();
    private static final String MAPPING_STAT_FILE_PATH = Paths.get("src", "test", "resources", "elastic.test.stat.mapping.json").toString();

    private static final int NUM_OF_DRAGONS_IN_INDEX_1 = 1000;
    private static final int NUM_OF_DRAGONS_IN_INDEX_2 = 555;
    private static final int NUM_OF_DRAGONS_IN_INDEX_3 = 200;
    private static final int NUM_OF_DRAGONS_IN_INDEX_4 = 100;

    private static final String STAT_INDEX_NAME = "stat";
    private static final String STAT_TYPE_NUMERIC_NAME = "bucketNumeric";
    private static final String STAT_TYPE_STRING_NAME = "bucketString";
    private static final String STAT_TYPE_TERM_NAME = "bucketTerm";
    private static final String STAT_TYPE_GLOBAL_NAME = "bucketGlobal";

    private static final String DATA_INDEX_NAME_1 = "index1";
    private static final String DATA_INDEX_NAME_2 = "index2";
    private static final String DATA_INDEX_NAME_3 = "index3";
    private static final String DATA_INDEX_NAME_4 = "index4";

    private static final String DATA_TYPE_DRAGON = "dragon";
    private static final String DATA_TYPE_FIRE = "fire";

    private static final String DATA_FIELD_NAME_AGE = "age";
    private static final String DATA_FIELD_NAME_ADDRESS = "address";
    private static final String DATA_FIELD_NAME_COLOR = "color";
    private static final String DATA_FIELD_NAME_GENDER = "gender";
    private static final String DATA_FIELD_NAME_TYPE = "_type";
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

        StatCalculator.main(new String[]{CONFIGURATION_FILE_PATH});
        Thread.sleep(3000);
        Set<Map<String, Object>> docs = StatTestUtil.searchByTerm(
                statClient,
                new String[]{STAT_INDEX_NAME},
                new String[]{STAT_TYPE_NUMERIC_NAME},
                "field", DATA_FIELD_NAME_TIMESTAMP);

        assertEquals(numOfBins, docs.size());
    }

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

        elasticEmbeddedNode = new ElasticEmbeddedNode(new ElasticIndexConfigurer[]{
                configurerIndex1,
                configurerIndex2,
                configurerIndex3,
                configurerIndex4,
                configurerStat});


        Thread.sleep(4000);

        new ElasticDataPopulator(
                dataClient,
                DATA_INDEX_NAME_1,
                DATA_TYPE_DRAGON,
                "id",
                () -> StatTestUtil.createDragons(NUM_OF_DRAGONS_IN_INDEX_1,
                        DRAGON_MIN_AGE,
                        DRAGON_MAX_AGE,
                        DRAGON_NAME_PREFIX_LENGTH,
                        DRAGON_COLORS,
                        DRAGON_GENDERS,
                        DRAGON_ADDRESS_LENGTH)).populate();

        new ElasticDataPopulator(
                dataClient,
                DATA_INDEX_NAME_2,
                DATA_TYPE_DRAGON,
                "id",
                () -> StatTestUtil.createDragons(NUM_OF_DRAGONS_IN_INDEX_2,
                        DRAGON_MIN_AGE,
                        DRAGON_MAX_AGE,
                        DRAGON_NAME_PREFIX_LENGTH,
                        DRAGON_COLORS,
                        DRAGON_GENDERS,
                        DRAGON_ADDRESS_LENGTH)).populate();


        new ElasticDataPopulator(
                dataClient,
                DATA_INDEX_NAME_3,
                DATA_TYPE_FIRE,
                "id",
                () -> StatTestUtil.createDragonFireDragonEdges(
                        NUM_OF_DRAGONS_IN_INDEX_3,
                        DRAGON_START_DATE,
                        DRAGON_END_DATE,
                        DRAGON_MIN_TEMP,
                        DRAGON_MAX_TEMP
                )).populate();

        new ElasticDataPopulator(
                dataClient,
                DATA_INDEX_NAME_4,
                DATA_TYPE_FIRE,
                "id",
                () -> StatTestUtil.createDragonFireDragonEdges(
                        NUM_OF_DRAGONS_IN_INDEX_4,
                        DRAGON_START_DATE,
                        DRAGON_END_DATE,
                        DRAGON_MIN_TEMP,
                        DRAGON_MAX_TEMP
                )).populate();

        Thread.sleep(4000);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (statClient != null) {
            statClient.close();
            statClient = null;
        }

        if (dataClient != null) {
            dataClient.close();
            dataClient = null;
        }

        elasticEmbeddedNode.close();
        Thread.sleep(4000);

    }
}
