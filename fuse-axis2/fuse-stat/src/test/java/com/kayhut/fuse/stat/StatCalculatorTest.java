package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.configuration.StatConfiguration;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.fuse.stat.util.EsUtil;
import com.kayhut.fuse.stat.util.StatUtil;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by benishue on 04-May-17.
 */
public class StatCalculatorTest {

    private static TransportClient dataClient;
    private static TransportClient statClient;
    private static ElasticEmbeddedNode elasticEmbeddedNode;
    private static final String CONFIGURATION_FILE_PATH = "statistics.test.properties";
    private static final int NUM_OF_DRAGONS_IN_INDEX_1 = 1000;
    private static final int NUM_OF_DRAGONS_IN_INDEX_2 = 555;
    private static final String STAT_INDEX_NAME = "stat";
    private static final String STAT_TYPE_NUMERIC_NAME = "bucketNumeric";
    private static final String STAT_TYPE_STRING_NAME = "bucketString";
    private static final String STAT_TYPE_TERM_NAME = "bucketTerm";
    private static final String DATA_INDEX_NAME_1 = "index1";
    private static final String DATA_INDEX_NAME_2 = "index2";
    private static final String DATA_TYPE_NAME_1 = "dragon";
    private static final String DATA_FIELD_NAME_AGE = "age";
    private static final String DATA_FIELD_NAME_ADDRESS = "address";
    private static final String DATA_FIELD_NAME_COLOR = "color";
    private static final String DATA_FIELD_NAME_GENDER = "gender";
    private static final String DATA_FIELD_NAME_TYPE = "_type";

    private static final int DRAGON_MIN_AGE = 0;
    private static final int DRAGON_MAX_AGE = 100;
    private static final int DRAGON_ADDRESS_LENGTH = 20;
    private static final int DRAGON_NAME_PREFIX_LENGTH = 10;
    private static final List<String> DRAGON_COLORS =
            Arrays.asList("red", "green", "yellow", "blue", "00", "11", "22", "33", "44", "55");
    private static final List<String> DRAGON_GENDERS =
            Arrays.asList("MALE", "FEMALE");


    //todo - add more tests, specially small unit tests per each case
    //Full - blown test using Statistics configuration File
    @Test
    public void statCalculatorTest() throws Exception {
        StatCalculator.main(new String[]{CONFIGURATION_FILE_PATH});

        //Check if Stat index created
        assertTrue(EsUtil.checkIfEsIndexExists(statClient, STAT_INDEX_NAME));
        assertTrue(EsUtil.checkIfEsTypeExists(statClient, STAT_INDEX_NAME, STAT_TYPE_NUMERIC_NAME));
        assertTrue(EsUtil.checkIfEsTypeExists(statClient, STAT_INDEX_NAME, STAT_TYPE_STRING_NAME));
        assertTrue(EsUtil.checkIfEsTypeExists(statClient, STAT_INDEX_NAME, STAT_TYPE_TERM_NAME));


        //Check if age stat numeric bucket exists (bucket #1: 10.0-19.0)
        String docId1 = StatUtil.hashString(DATA_INDEX_NAME_1 + DATA_TYPE_NAME_1 + DATA_FIELD_NAME_AGE + "10.0" + "19.0");
        assertTrue(EsUtil.checkIfEsDocExists(statClient, STAT_INDEX_NAME, STAT_TYPE_NUMERIC_NAME, docId1));

        //Check if address bucket exists (bucket #1 "abc" + "dzz")
        String docId2 = StatUtil.hashString(DATA_INDEX_NAME_1 + DATA_TYPE_NAME_1 + DATA_FIELD_NAME_ADDRESS + "abc" + "dzz");
        assertTrue(EsUtil.checkIfEsDocExists(statClient, STAT_INDEX_NAME, STAT_TYPE_STRING_NAME, docId2));

        //Check if color bucket exists (bucket with lower_bound: "grc", upper_bound: "grl")
        String docId3 = StatUtil.hashString(DATA_INDEX_NAME_1 + DATA_TYPE_NAME_1 + DATA_FIELD_NAME_COLOR + "grc" + "grl");
        assertTrue(EsUtil.checkIfEsDocExists(statClient, STAT_INDEX_NAME, STAT_TYPE_STRING_NAME, docId3));

        //Check that the bucket ["grc", "grl") have the cardinality of 1 (i.e. Green Color)
        Optional<Map<String, Object>> doc3Result = EsUtil.getDocumentSourceById(statClient, STAT_INDEX_NAME, STAT_TYPE_STRING_NAME, docId3);
        assertTrue(doc3Result.isPresent());
        assertEquals(1, (int) doc3Result.get().get("cardinality"));

        //Check that the manual bucket ("00", "11"] exists for the composite histogram
        String docId4 = StatUtil.hashString(DATA_INDEX_NAME_1 + DATA_TYPE_NAME_1 + DATA_FIELD_NAME_COLOR + "00" + "11");
        assertTrue(EsUtil.checkIfEsDocExists(statClient, STAT_INDEX_NAME, STAT_TYPE_STRING_NAME, docId4));

        //Check term buckets (Gender male) - cardinality should be 1
        String docId5 = StatUtil.hashString(DATA_INDEX_NAME_1 + DATA_TYPE_NAME_1 + DATA_FIELD_NAME_GENDER + "male");
        Optional<Map<String, Object>> doc5Result = EsUtil.getDocumentSourceById(statClient, STAT_INDEX_NAME, STAT_TYPE_TERM_NAME, docId5);
        assertTrue(doc5Result.isPresent());
        assertEquals(1, (int) doc5Result.get().get("cardinality"));
        //Since we have 1000 dragons (~0.5% should be males)
        assertEquals(Double.valueOf(doc5Result.get().get("count").toString()),
                NUM_OF_DRAGONS_IN_INDEX_1 / 2.0, NUM_OF_DRAGONS_IN_INDEX_1 * 0.1);

        //Check that there are 1000 dragons in Index: "index1", Type: "dragon"
        //Cardinality should be 1
        String docId6 = StatUtil.hashString(DATA_INDEX_NAME_1 + DATA_TYPE_NAME_1 + DATA_FIELD_NAME_TYPE + "dragon");
        Optional<Map<String, Object>> doc6Result = EsUtil.getDocumentSourceById(statClient, STAT_INDEX_NAME, STAT_TYPE_TERM_NAME, docId6);
        assertTrue(doc6Result.isPresent());
        assertEquals(1, (int) doc6Result.get().get("cardinality"));
        assertEquals((int) doc6Result.get().get("count"), 1000);
    }

    @BeforeClass
    public static void setup() throws Exception {

        Configuration configuration = new StatConfiguration(CONFIGURATION_FILE_PATH).getInstance();

        dataClient = ClientProvider.getDataClient(configuration);
        statClient = ClientProvider.getDataClient(configuration);
        elasticEmbeddedNode = new ElasticEmbeddedNode();

        Thread.sleep(4000);

        new ElasticDataPopulator(
                dataClient,
                DATA_INDEX_NAME_1,
                DATA_TYPE_NAME_1,
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
                DATA_TYPE_NAME_1,
                "id",
                () -> StatTestUtil.createDragons(NUM_OF_DRAGONS_IN_INDEX_2,
                        DRAGON_MIN_AGE,
                        DRAGON_MAX_AGE,
                        DRAGON_NAME_PREFIX_LENGTH,
                        DRAGON_COLORS,
                        DRAGON_GENDERS,
                        DRAGON_ADDRESS_LENGTH)).populate();

        Thread.sleep(2000);

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