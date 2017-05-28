package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.configuration.StatConfiguration;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.bucket.BucketTerm;
import com.kayhut.fuse.stat.model.configuration.Field;
import com.kayhut.fuse.stat.model.configuration.Mapping;
import com.kayhut.fuse.stat.model.configuration.StatContainer;
import com.kayhut.fuse.stat.model.configuration.Type;
import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.histogram.*;
import com.kayhut.fuse.stat.util.EsUtil;
import com.kayhut.fuse.stat.util.StatTestUtil;
import com.kayhut.fuse.stat.util.StatUtil;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.index.ElasticIndexConfigurer;
import com.kayhut.test.framework.index.MappingFileElasticConfigurer;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by benishue on 04-May-17.
 */
public class StatCalculatorTest {

    private static TransportClient dataClient;
    private static TransportClient statClient;
    private static ElasticEmbeddedNode elasticEmbeddedNode;
    private static final String CONFIGURATION_FILE_PATH = "statistics.test.properties";
    private static final String MAPPING_DATA_FILE_PATH = "src\\test\\resources\\elastic.test.data.mapping.json";
    private static final String MAPPING_STAT_FILE_PATH = "src\\test\\resources\\elastic.test.stat.mapping.json";

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
            Arrays.asList("male", "female");


    @Rule
    public final ExpectedException exception = ExpectedException.none();

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


    @Test
    public void statCalculatorInvalidArgumentsTest() {
        try {
            StatCalculator.main(new String[]{});
            fail("Exception not thrown");
        } catch (Exception expected) {
          // we should have reach here
        }
    }

    @BeforeClass
    public static void setup() throws Exception {

        Configuration configuration = new StatConfiguration(CONFIGURATION_FILE_PATH).getInstance();

        dataClient = ClientProvider.getDataClient(configuration);
        statClient = ClientProvider.getDataClient(configuration);

        MappingFileElasticConfigurer configurerIndex1 = new MappingFileElasticConfigurer(DATA_INDEX_NAME_1, MAPPING_DATA_FILE_PATH);
        MappingFileElasticConfigurer configurerIndex2 = new MappingFileElasticConfigurer(DATA_INDEX_NAME_2, MAPPING_DATA_FILE_PATH);
        MappingFileElasticConfigurer configurerStat = new MappingFileElasticConfigurer(STAT_INDEX_NAME, MAPPING_STAT_FILE_PATH);

        elasticEmbeddedNode = new ElasticEmbeddedNode(new ElasticIndexConfigurer[]{configurerIndex1, configurerIndex2, configurerStat});


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

    private StatContainer buildStatContainer() {
        HistogramNumeric histogramDragonAge = HistogramNumeric.Builder.aHistogramNumeric()
                .withMin(10).withMax(100).withNumOfBins(10).build();

        HistogramString histogramDragonName = HistogramString.Builder.aHistogramString()
                .withPrefixSize(3)
                .withInterval(10).withNumOfChars(26).withFirstCharCode("97").build();

        HistogramManual histogramDragonAddress = HistogramManual.Builder.aHistogramManual()
                .withBuckets(Arrays.asList(
                        new BucketRange("abc", "dzz"),
                        new BucketRange("efg", "hij"),
                        new BucketRange("klm", "xyz")
                )).withDataType(DataType.string)
                .build();

        HistogramComposite histogramDragonColor = HistogramComposite.Builder.aHistogramComposite()
                .withManualBuckets(Arrays.asList(
                        new BucketRange("00", "11"),
                        new BucketRange("22", "33"),
                        new BucketRange("44", "55")
                )).withDataType(DataType.string)
                .withAutoBuckets(HistogramString.Builder.aHistogramString()
                        .withFirstCharCode("97")
                        .withInterval(10)
                        .withNumOfChars(26)
                        .withPrefixSize(3).build())
                .build();

        HistogramTerm histogramTerm = HistogramTerm.Builder.aHistogramTerm()
                .withDataType(DataType.string).withBuckets(Arrays.asList(
                        new BucketTerm("male"),
                        new BucketTerm("female")
                )).build();

        HistogramTerm histogramDocType = HistogramTerm.Builder.aHistogramTerm()
                .withDataType(DataType.string).withBuckets(Collections.singletonList(
                        new BucketTerm("dragon")
                )).build();


        Field nameField = new Field("name", histogramDragonName);
        Field ageField = new Field("age", histogramDragonAge);
        Field addressField = new Field("address", histogramDragonAddress);
        Field colorField = new Field("color", histogramDragonColor);
        Field genderField = new Field("gender", histogramTerm);
        Field dragonTypeField = new Field("_type", histogramDocType);


        Type typeDragon = new Type("dragon", Arrays.asList(ageField, nameField, addressField, colorField, genderField, dragonTypeField));

        Mapping mapping = Mapping.MappingBuilder.aMapping().withIndices(Arrays.asList("index1", "index2"))
                .withTypes(Collections.singletonList("dragon")).build();

        return StatContainer.Builder.aStatContainer()
                .withMappings(Collections.singletonList(mapping))
                .withTypes(Collections.singletonList(typeDragon))
                .build();
    }
}