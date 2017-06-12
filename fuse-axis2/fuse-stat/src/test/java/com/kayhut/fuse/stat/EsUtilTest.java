package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.configuration.StatConfiguration;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.bucket.BucketTerm;
import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.result.StatRangeResult;
import com.kayhut.fuse.stat.model.result.StatTermResult;
import com.kayhut.fuse.stat.util.EsUtil;
import com.kayhut.fuse.stat.util.StatTestUtil;
import com.kayhut.fuse.stat.util.StatUtil;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import javaslang.collection.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by benishue on 24/05/2017.
 */
public class EsUtilTest {

    private static TransportClient dataClient;
    private static TransportClient statClient;
    private static ElasticEmbeddedNode elasticEmbeddedNode;
    private static Iterable<Map<String, Object>> dragonsList;

    private static final String CONFIGURATION_FILE_PATH = "statistics.test.properties";
    private static final int NUM_OF_DRAGONS_IN_INDEX = 1000;
    private static final String STAT_INDEX_NAME = "stat";
    private static final String STAT_TYPE_NUMERIC_NAME = "bucketNumeric";
    private static final String STAT_TYPE_STRING_NAME = "bucketString";
    private static final String STAT_TYPE_TERM_NAME = "bucketTerm";
    private static final String DATA_INDEX_NAME = "index1";
    private static final String DATA_TYPE_NAME = "Dragon";
    private static final String DATA_FIELD_NAME_AGE = "age";
    private static final String DATA_FIELD_NAME_ADDRESS = "address";
    private static final String DATA_FIELD_NAME_COLOR = "color";
    private static final String DATA_FIELD_NAME_GENDER = "gender";
    private static final String DATA_FIELD_NAME_TYPE = "_type";

    private static final int DRAGON_MIN_AGE = 1;
    private static final int DRAGON_MAX_AGE = 10;
    private static final int DRAGON_ADDRESS_LENGTH = 20;
    private static final int DRAGON_NAME_PREFIX_LENGTH = 10;
    private static final List<String> DRAGON_COLORS =
            Arrays.asList("red", "green", "yellow", "blue");
    private static final List<String> DRAGON_GENDERS =
            Arrays.asList("male", "female");


    @Test
    public void getNumericHistogramResultsTest() throws Exception {
        final int numOfBins = 10;
        final double min = DRAGON_MIN_AGE;
        final double max = DRAGON_MAX_AGE;

        List<BucketRange<Double>> numericBuckets = StatUtil.createNumericBuckets(
                min,
                max,
                numOfBins);
        List<StatRangeResult> numericHistogramResults = EsUtil.getNumericHistogramResults(
                dataClient,
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                DATA_FIELD_NAME_AGE,
                numericBuckets);

        //Checking that we have 1o buckets
        assertEquals(numOfBins, numericHistogramResults.size());
        //Checking that the lower bound of the left most bucket is MIN
        assertEquals(min, numericHistogramResults.get(0).getLowerBound());
        //Checking that the upper bound of the right most bucket is MAX
        assertEquals(max, numericHistogramResults.get(numOfBins - 1).getUpperBound());

        //Comparing our function of buckets creation with the one of Apache Math
        double[] data = {min, 8.2, 6.333, 1.4, 1.5, 4.2, 7.3, 9.4, 1.1, max};
        EmpiricalDistribution distribution = new EmpiricalDistribution(numOfBins);
        distribution.load(data);

        List<Double> upperBoundsApache = Stream.ofAll(distribution.getUpperBounds()).sorted().toJavaList();
        List<Double> upperBoundsOurs = Stream.ofAll(numericHistogramResults).map(statRangeResult -> (Double) statRangeResult.getUpperBound()).sorted().toJavaList();
        assertTrue(CollectionUtils.isEqualCollection(upperBoundsApache, upperBoundsOurs));

        /*
        Selecting random bin and checking that the number  of elements
        in the bucket is approximately ~ NumOfDragons/NumOfBuckets
        */
        for (int i = 0; i < 10; i++) {
            StatRangeResult statRangeResult = numericHistogramResults.get(new Random().nextInt(numericHistogramResults.size()));
            assertEquals(statRangeResult.getDocCount(), NUM_OF_DRAGONS_IN_INDEX / numOfBins, NUM_OF_DRAGONS_IN_INDEX * 0.2);
        }
    }

    @Test
    public void getManualHistogramResultsTest() throws Exception {
        BucketRange bucketRange_1dot0_TO_1dot5 = new BucketRange(1.0, 1.5);
        List<BucketRange<Double>> manualBuckets = Arrays.asList(
                bucketRange_1dot0_TO_1dot5,
                new BucketRange<>(1.5, 2.0),
                new BucketRange<>(2.0, 2.5)
        );

        StatRangeResult statRangeResult_1dot0_TO_1dot5 = new StatRangeResult(DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                DATA_FIELD_NAME_AGE,
                "don't care",
                DataType.numeric,
                bucketRange_1dot0_TO_1dot5.getStart(),
                bucketRange_1dot0_TO_1dot5.getEnd(),
                0,
                0);

        dragonsList.forEach(dragon -> {
            double age = ((Number) dragon.get(DATA_FIELD_NAME_AGE)).doubleValue();
            double lowerBoundFirstBucket = ((Number) bucketRange_1dot0_TO_1dot5.getStart()).doubleValue();
            double upperBoundFirstBucket = ((Number) bucketRange_1dot0_TO_1dot5.getEnd()).doubleValue();

            if (age >= lowerBoundFirstBucket && age < upperBoundFirstBucket) {
                statRangeResult_1dot0_TO_1dot5.setDocCount(statRangeResult_1dot0_TO_1dot5.getDocCount() + 1);
            }
        });

        List<StatRangeResult> manualHistogramResults = EsUtil.getManualHistogramResults(dataClient,
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                DATA_FIELD_NAME_AGE,
                DataType.numeric,
                manualBuckets);
        assertEquals(manualBuckets.size(), manualHistogramResults.size());

        assertEquals(statRangeResult_1dot0_TO_1dot5.getDocCount(), manualHistogramResults.get(0).getDocCount());

    }

    @Test
    public void getStringBucketsStatResultsTest() throws Exception {
        BucketRange<String> stringBucketRange_A_TO_B = new BucketRange<>("a", "b");
        List<BucketRange<String>> stringBuckets = Arrays.asList(
                stringBucketRange_A_TO_B,
                new BucketRange<>("c", "d"));
        StatRangeResult statRangeResult_A_TO_B = new StatRangeResult(DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                DATA_FIELD_NAME_ADDRESS,
                "don't care",
                DataType.string,
                stringBucketRange_A_TO_B.getStart(),
                stringBucketRange_A_TO_B.getEnd(),
                0,
                0);


        List<StatRangeResult> stringBucketsStatResults = EsUtil.getStringBucketsStatResults(dataClient,
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                DATA_FIELD_NAME_ADDRESS,
                stringBuckets);

        dragonsList.forEach(dragon -> {
            String address = dragon.get(DATA_FIELD_NAME_ADDRESS).toString();
            if (address.startsWith("a")) {
                statRangeResult_A_TO_B.setDocCount(statRangeResult_A_TO_B.getDocCount() + 1);
            }
        });
        assertEquals(stringBuckets.size(), stringBucketsStatResults.size());
        assertEquals(statRangeResult_A_TO_B.getDocCount(), stringBucketsStatResults.get(0).getDocCount());
    }


    @Test
    public void getTermHistogramResultsTest() throws Exception {
        String randomGender = DRAGON_GENDERS.get(StatTestUtil.randomInt(0, DRAGON_GENDERS.size() - 1));
        BucketTerm bucketTerm = new BucketTerm(randomGender);
        List<StatTermResult> termHistogramResults = EsUtil.getTermHistogramResults(dataClient,
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                DATA_FIELD_NAME_GENDER,
                DataType.string,
                Collections.singletonList(bucketTerm)
        );

        //We have only one bucket
        assertEquals(1, termHistogramResults.size());
        //We should get proportional (~equal) docCount for each gender
        assertEquals(NUM_OF_DRAGONS_IN_INDEX / (double) DRAGON_GENDERS.size(),
                termHistogramResults.get(0).getDocCount(),
                NUM_OF_DRAGONS_IN_INDEX * 0.05);
    }

    @Test
    public void checkIfEsIndexExistsTest() throws Exception {
        assertTrue(EsUtil.isIndexExists(dataClient, DATA_INDEX_NAME));
    }

    @Test
    public void checkIfEsTypeExistsTest() throws Exception {
        assertTrue(EsUtil.isTypeExists(dataClient, DATA_INDEX_NAME, DATA_TYPE_NAME));

    }

    @Test
    public void checkIfEsDocExistsTest() throws Exception {
        assertTrue(EsUtil.isDocExists(dataClient,
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                Integer.toString(StatTestUtil.randomInt(0, NUM_OF_DRAGONS_IN_INDEX - 1))
        ));
    }

    @Test
    public void getDocumentByIdTest() throws Exception {
        for (int i = 0; i < NUM_OF_DRAGONS_IN_INDEX; i++) {
            Optional<Map<String, Object>> documentById = EsUtil.getDocumentSourceById(dataClient, DATA_INDEX_NAME, DATA_TYPE_NAME, Integer.toString(i));
            assertTrue(documentById.isPresent());
        }
    }

    @Test
    public void getDocumentTypeByDocIdTest() throws Exception {
        for (int i = 0; i < NUM_OF_DRAGONS_IN_INDEX; i++) {
            Optional<String> documentTypeByDocId = EsUtil.getDocumentTypeByDocId(dataClient
                    , DATA_INDEX_NAME,
                    DATA_TYPE_NAME,
                    Integer.toString(i));

            assertTrue(documentTypeByDocId.isPresent());
            assertEquals(DATA_TYPE_NAME, documentTypeByDocId.get());
        }
    }

    @BeforeClass
    public static void setup() throws Exception {

        Configuration configuration = new StatConfiguration(CONFIGURATION_FILE_PATH).getInstance();

        dataClient = ClientProvider.getDataClient(configuration);
        statClient = ClientProvider.getDataClient(configuration);
        elasticEmbeddedNode = new ElasticEmbeddedNode();

        dragonsList = StatTestUtil.createDragons(NUM_OF_DRAGONS_IN_INDEX,
                DRAGON_MIN_AGE,
                DRAGON_MAX_AGE,
                DRAGON_NAME_PREFIX_LENGTH,
                DRAGON_COLORS,
                DRAGON_GENDERS,
                DRAGON_ADDRESS_LENGTH);

        new ElasticDataPopulator(
                dataClient,
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                "id",
                () -> dragonsList
        ).populate();

        dataClient.admin().indices().refresh(new RefreshRequest(DATA_INDEX_NAME)).actionGet();
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