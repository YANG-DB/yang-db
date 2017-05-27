package com.kayhut.fuse.stat;

import com.kayhut.fuse.stat.configuration.StatConfiguration;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.result.StatRangeResult;
import com.kayhut.fuse.stat.util.EsUtil;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import javaslang.collection.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by benishue on 24/05/2017.
 */
public class EsUtilTest {

    private static TransportClient dataClient;
    private static TransportClient statClient;
    private static ElasticEmbeddedNode elasticEmbeddedNode;

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
            Arrays.asList("MALE", "FEMALE");


    @Test
    public void getNumericHistogramResults() throws Exception {
        final int numOfBins = 10;
        final double min = DRAGON_MIN_AGE;
        final double max = DRAGON_MAX_AGE;

        List<StatRangeResult> numericHistogramResults = EsUtil.getNumericHistogramResults(
                dataClient,
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                DATA_FIELD_NAME_AGE,
                min,
                max,
                numOfBins);

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
        StatRangeResult statRangeResult = numericHistogramResults.get(new Random().nextInt(numericHistogramResults.size()));
        assertEquals(statRangeResult.getDocCount(), NUM_OF_DRAGONS_IN_INDEX / numOfBins, NUM_OF_DRAGONS_IN_INDEX * 0.2);

    }

    @Test
    public void getManualHistogramResults() throws Exception {
        List<BucketRange<Double>> manualBuckets = Arrays.asList(
                new BucketRange<>(1.0, 1.5),
                new BucketRange<>(1.5, 2.0),
                new BucketRange<>(2.0, 2.5)
        );
        List<StatRangeResult> manualHistogramResults = EsUtil.getManualHistogramResults(dataClient,
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                DATA_FIELD_NAME_AGE,
                DataType.numeric,
                manualBuckets);
        assertEquals(manualBuckets.size(), manualHistogramResults.size());
    }

    @Test
    public void getStringBucketsStatResults() throws Exception {

    }

    @Test
    public void getTermHistogramResults() throws Exception {

    }

    @Test
    public void checkIfEsIndexExists() throws Exception {

    }

    @Test
    public void checkIfEsTypeExists() throws Exception {

    }

    @Test
    public void checkIfEsDocExists() throws Exception {

    }

    @Test
    public void getDocumentById() throws Exception {

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
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                "id",
                () -> StatTestUtil.createDragons(NUM_OF_DRAGONS_IN_INDEX,
                        DRAGON_MIN_AGE,
                        DRAGON_MAX_AGE,
                        DRAGON_NAME_PREFIX_LENGTH,
                        DRAGON_COLORS,
                        DRAGON_GENDERS,
                        DRAGON_ADDRESS_LENGTH
                )).populate();

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