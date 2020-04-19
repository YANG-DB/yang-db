package com.yangdb.fuse.stat;

import com.yangdb.fuse.stat.configuration.StatConfiguration;
import com.yangdb.fuse.stat.util.EsUtil;
import com.yangdb.fuse.stat.util.StatTestUtil;
import com.yangdb.fuse.stat.util.StatUtil;
import com.yangdb.fuse.test.framework.index.MappingFileElasticConfigurer;
import com.yangdb.fuse.test.framework.populator.ElasticDataPopulator;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.junit.Assert.*;
import static com.yangdb.fuse.stat.StatTestSuite.*;

/**
 * Created by benishue on 04-May-17.
 */
public class StatCalculatorTest {
    private static final String CONFIGURATION_FILE_PATH = "statistics.test.properties";

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

    private static final String DATA_TYPE_DRAGON = "Dragon";
    private static final String DATA_TYPE_FIRE = "fire";

    private static final String DATA_FIELD_NAME_AGE = "age";
    private static final String DATA_FIELD_NAME_ADDRESS = "address";
    private static final String DATA_FIELD_NAME_COLOR = "color";
    private static final String DATA_FIELD_NAME_GENDER = "gender";
    private static final String DATA_FIELD_NAME_TYPE = "type";

    private static final int DRAGON_MIN_AGE = 0;
    private static final int DRAGON_MAX_AGE = 100;
    private static final int DRAGON_ADDRESS_LENGTH = 20;
    private static final int DRAGON_NAME_PREFIX_LENGTH = 10;
    private static final int DRAGON_MIN_TEMP = 25;
    private static final int DRAGON_MAX_TEMP = 1000;
    private static final long DRAGON_START_DATE = 0L;
    private static final long DRAGON_END_DATE = 999999L;
    private static final List<String> DRAGON_COLORS =
            Arrays.asList("red", "green", "yellow", "blue", "00", "11", "22", "33", "44", "55");
    private static final List<String> DRAGON_GENDERS =
            Arrays.asList("male", "female");


    @Rule
    public final ExpectedException exception = ExpectedException.none();

    //Full - blown test using Statistics configuration File
    @Test
    public void statCalculatorTest() throws Exception {
        StatCalculator.run(dataClient, statClient, new StatConfiguration(CONFIGURATION_FILE_PATH).getInstance());
        statClient.admin().indices().refresh(new RefreshRequest(STAT_INDEX_NAME)).actionGet();

        //Check if Stat index created
        assertTrue(EsUtil.isIndexExists(statClient, STAT_INDEX_NAME));
        assertTrue(EsUtil.isTypeExists(statClient, STAT_INDEX_NAME, STAT_TYPE_NUMERIC_NAME));
        assertTrue(EsUtil.isTypeExists(statClient, STAT_INDEX_NAME, STAT_TYPE_STRING_NAME));
        assertTrue(EsUtil.isTypeExists(statClient, STAT_INDEX_NAME, STAT_TYPE_TERM_NAME));
        assertTrue(EsUtil.isTypeExists(statClient, STAT_INDEX_NAME, STAT_TYPE_GLOBAL_NAME));


        //Check if age stat numeric bucket exists (bucket #1: 10.0-19.0)
        String docId1 = StatUtil.hashString(DATA_INDEX_NAME_1 + DATA_TYPE_DRAGON + DATA_FIELD_NAME_AGE + "10.0" + "19.0");
        assertTrue(EsUtil.isDocExists(statClient, STAT_INDEX_NAME, STAT_TYPE_NUMERIC_NAME, docId1));

        //Check if address bucket exists (bucket #1 "abc" + "dzz")
        String docId2 = StatUtil.hashString(DATA_INDEX_NAME_1 + DATA_TYPE_DRAGON + DATA_FIELD_NAME_ADDRESS + "abc" + "dzz");
        assertTrue(EsUtil.isDocExists(statClient, STAT_INDEX_NAME, STAT_TYPE_STRING_NAME, docId2));

        //Check if color bucket exists (bucket with lower_bound: "grc", upper_bound: "grl")
        String docId3 = StatUtil.hashString(DATA_INDEX_NAME_1 + DATA_TYPE_DRAGON + DATA_FIELD_NAME_COLOR + "gqg" + "grq");
        assertTrue(EsUtil.isDocExists(statClient, STAT_INDEX_NAME, STAT_TYPE_STRING_NAME, docId3));

        //Check that the bucket ["grc", "grl") have the cardinality of 1 (i.e. Green Color)
        Optional<Map<String, Object>> doc3Result = EsUtil.getDocumentSourceById(statClient, STAT_INDEX_NAME, STAT_TYPE_STRING_NAME, docId3);
        assertTrue(doc3Result.isPresent());
        assertEquals(1, (int) doc3Result.get().get("cardinality"));

        //Check that the manual bucket ("00", "11"] exists for the composite histogram
        String docId4 = StatUtil.hashString(DATA_INDEX_NAME_1 + DATA_TYPE_DRAGON + DATA_FIELD_NAME_COLOR + "00" + "11");
        assertTrue(EsUtil.isDocExists(statClient, STAT_INDEX_NAME, STAT_TYPE_STRING_NAME, docId4));

        //Check term buckets (Gender male) - cardinality should be 1
        String docId5 = StatUtil.hashString(DATA_INDEX_NAME_1 + DATA_TYPE_DRAGON + DATA_FIELD_NAME_GENDER + "male");
        Optional<Map<String, Object>> doc5Result = EsUtil.getDocumentSourceById(statClient, STAT_INDEX_NAME, STAT_TYPE_TERM_NAME, docId5);
        assertTrue(doc5Result.isPresent());
        assertEquals(1, (int) doc5Result.get().get("cardinality"));
        //Since we have 1000 dragons (~0.5% should be males)
        assertEquals(Double.valueOf(doc5Result.get().get("count").toString()),
                NUM_OF_DRAGONS_IN_INDEX_1 / 2.0, NUM_OF_DRAGONS_IN_INDEX_1 * 0.1);

        //Check that there are 1000 dragons in Index: "index1", Type: "dragon"
        //Cardinality should be 1
        String docId6 = StatUtil.hashString(DATA_INDEX_NAME_1 + DATA_TYPE_DRAGON + DATA_FIELD_NAME_TYPE + "Dragon");
        Optional<Map<String, Object>> doc6Result = EsUtil.getDocumentSourceById(statClient, STAT_INDEX_NAME, STAT_TYPE_TERM_NAME, docId6);
        assertTrue(doc6Result.isPresent());
        assertEquals(1, (int) doc6Result.get().get("cardinality"));
        assertEquals((int) doc6Result.get().get("count"), 1000);
    }

    @Test
    public void globalCardinalityTest() throws Exception {
        //Check to see if the mapping for Stat results for global was created
        StatCalculator.run(dataClient, statClient, new StatConfiguration(CONFIGURATION_FILE_PATH).getInstance());
        statClient.admin().indices().refresh(new RefreshRequest(STAT_INDEX_NAME)).actionGet();

        StatTestUtil.printAllDocs(statClient, STAT_INDEX_NAME, STAT_TYPE_GLOBAL_NAME);
    }

    @Test
    public void statCalculatorInvalidArgumentsTest() {
        try {
            StatCalculator.main(new String[]{});
            fail("Exception not thrown");
        } catch (Exception expected) {
            // we should have reach here
        }
    }

    @Test
    public void statDataValidationTest() throws Exception {
        //Used only to test that the data is populated and indexed correctly

        //Check if all indices exists: index1, index2, index3, index4
        assertTrue(EsUtil.isIndexExists(dataClient, DATA_INDEX_NAME_1));
        assertTrue(EsUtil.isIndexExists(dataClient, DATA_INDEX_NAME_2));
        assertTrue(EsUtil.isIndexExists(dataClient, DATA_INDEX_NAME_3));
        assertTrue(EsUtil.isIndexExists(dataClient, DATA_INDEX_NAME_4));

        //Check if Type= Dragon exists in indices: index1, index2
        assertTrue(EsUtil.isTypeExists(dataClient, DATA_INDEX_NAME_1, DATA_TYPE_DRAGON));
        assertTrue(EsUtil.isTypeExists(dataClient, DATA_INDEX_NAME_2, DATA_TYPE_DRAGON));

        //Check if Type= Dragon exists in indices: index1, index2
        assertTrue(EsUtil.isTypeExists(dataClient, DATA_INDEX_NAME_3, DATA_TYPE_FIRE));
        assertTrue(EsUtil.isTypeExists(dataClient, DATA_INDEX_NAME_4, DATA_TYPE_FIRE));

        //Check that we have documents and they are in the right place
        assertTrue(EsUtil.getFirstNDocumentsInType(dataClient, DATA_INDEX_NAME_1, DATA_TYPE_DRAGON, 10).getHits().getTotalHits().value > 0);
        assertTrue(EsUtil.getFirstNDocumentsInType(dataClient, DATA_INDEX_NAME_2, DATA_TYPE_DRAGON, 10).getHits().getTotalHits().value > 0);
        assertTrue(EsUtil.getFirstNDocumentsInType(dataClient, DATA_INDEX_NAME_3, DATA_TYPE_FIRE, 10).getHits().getTotalHits().value > 0);
        assertTrue(EsUtil.getFirstNDocumentsInType(dataClient, DATA_INDEX_NAME_4, DATA_TYPE_FIRE, 10).getHits().getTotalHits().value > 0);

        assertTrue(EsUtil.getFirstNDocumentsInType(dataClient, DATA_INDEX_NAME_1, DATA_TYPE_FIRE, 10).getHits().getTotalHits().value == 0);
        assertTrue(EsUtil.getFirstNDocumentsInType(dataClient, DATA_INDEX_NAME_2, DATA_TYPE_FIRE, 10).getHits().getTotalHits().value == 0);
        assertTrue(EsUtil.getFirstNDocumentsInType(dataClient, DATA_INDEX_NAME_3, DATA_TYPE_DRAGON, 10).getHits().getTotalHits().value == 0);
        assertTrue(EsUtil.getFirstNDocumentsInType(dataClient, DATA_INDEX_NAME_4, DATA_TYPE_DRAGON, 10).getHits().getTotalHits().value == 0);

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