package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.epb.plan.statistics.util.ElasticStatUtil;
import com.kayhut.fuse.epb.util.EpbTestUtil;
import com.kayhut.fuse.stat.StatCalculator;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.bucket.BucketTerm;
import com.kayhut.fuse.stat.model.configuration.Field;
import com.kayhut.fuse.stat.model.configuration.Mapping;
import com.kayhut.fuse.stat.model.configuration.StatContainer;
import com.kayhut.fuse.stat.model.configuration.Type;
import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.histogram.*;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import com.kayhut.test.framework.index.ElasticIndexConfigurer;
import com.kayhut.test.framework.index.MappingFileElasticConfigurer;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by benishue on 28-May-17.
 */
public class ElasticStatUtilTest {

    //region Parameters
    private static TransportClient dataClient;
    private static TransportClient statClient;
    private static ElasticEmbeddedNode elasticEmbeddedNode;
    private static Iterable<Map<String, Object>> dragonsList;

    private static final String MAPPING_DATA_FILE_PATH = "src\\test\\resources\\elastic.test.data.mapping.json";
    private static final String MAPPING_STAT_FILE_PATH = "src\\test\\resources\\elastic.test.stat.mapping.json";
    private static final String CONFIGURATION_FILE_PATH = "statistics.test.properties";

    private static final int NUM_OF_DRAGONS_IN_INDEX = 1000;
    private static final String STAT_INDEX_NAME = "stat";
    private static final String STAT_TYPE_NUMERIC_NAME = "bucketNumeric";
    private static final String STAT_TYPE_STRING_NAME = "bucketString";
    private static final String STAT_TYPE_TERM_NAME = "bucketTerm";
    private static final String DATA_INDEX_NAME = "index1";
    private static final String DATA_TYPE_NAME = "Dragon";
    private static final String DATA_FIELD_NAME_AGE = "age";
    private static final String DATA_FIELD_NAME_NAME = "name";
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


    // The name of the Elastic cluster
    private static final String DATA_CLUSTER_NAME = "fuse.test_elastic";

    // A list of hostnames for of the nodes in the cluster
    private static final String[] DATA_HOSTS = new String[]{"localhost"};

    // The transport port for the cluster
    private static final int DATA_TRANSPORT_PORT = 9300;

    // The name of the statistics cluster
    private static final String STAT_CLUSTER_NAME = "fuse.test_elastic";

    //A list of hostnames for of the nodes in the statistics cluster
    private static final String[] STAT_HOSTS = new String[]{"localhost"};

    // The transport port for the statistics cluster
    private static final int STAT_TRANSPORT_PORT = 9300;

    static Logger logger = org.slf4j.LoggerFactory.getLogger(ElasticStatUtilTest.class);
    //endregion

    //todo Add more tests

    @Test
    public void getGenderFieldStatisticsTest() throws Exception {
        List<Statistics.BucketInfo> genderStatistics = ElasticStatUtil.getFieldStatistics(statClient,
                STAT_INDEX_NAME,
                STAT_TYPE_TERM_NAME,
                Arrays.asList(DATA_INDEX_NAME),
                Arrays.asList(DATA_TYPE_NAME),
                Arrays.asList(DATA_FIELD_NAME_GENDER));



        assertEquals(DRAGON_GENDERS.size(), genderStatistics.size());
        assertNotNull(genderStatistics.get(0));
        //Cardinality of specific gender should be 1
        assertEquals(new Long(1), genderStatistics.get(0).getCardinality());
        genderStatistics.forEach(bucketInfo -> {
            //Since this is a term: Lower bound value == Upper bound value
            assertEquals(bucketInfo.getLowerBound(), bucketInfo.getHigherBound());
        });
    }


    @Test
    public void getAgeFieldStatisticsTest() throws Exception {
        List<Statistics.BucketInfo> ageStatistics = ElasticStatUtil.getFieldStatistics(statClient,
                STAT_INDEX_NAME,
                STAT_TYPE_NUMERIC_NAME,
                Arrays.asList(DATA_INDEX_NAME),
                Arrays.asList(DATA_TYPE_NAME),
                Arrays.asList(DATA_FIELD_NAME_AGE));

        assertTrue(ageStatistics.size() > 0);

        ageStatistics.forEach(bucketInfo -> {
            //Since this is a term: Lower bound value == Upper bound value
            assertTrue((Double)bucketInfo.getLowerBound() <= (Double)bucketInfo.getHigherBound());
        });
    }


    @BeforeClass
    public static void setUp() throws Exception {
        MappingFileElasticConfigurer configurerIndex1 = new MappingFileElasticConfigurer(DATA_INDEX_NAME, MAPPING_DATA_FILE_PATH);
        MappingFileElasticConfigurer configurerStat = new MappingFileElasticConfigurer(STAT_INDEX_NAME, MAPPING_STAT_FILE_PATH);

        elasticEmbeddedNode = new ElasticEmbeddedNode(new ElasticIndexConfigurer[]{configurerIndex1, configurerStat});


        dataClient = ClientProvider.getTransportClient(DATA_CLUSTER_NAME, DATA_TRANSPORT_PORT, DATA_HOSTS);
        statClient = ClientProvider.getTransportClient(STAT_CLUSTER_NAME, STAT_TRANSPORT_PORT, STAT_HOSTS);

        dragonsList = EpbTestUtil.createDragons(NUM_OF_DRAGONS_IN_INDEX,
                DRAGON_MIN_AGE,
                DRAGON_MAX_AGE,
                DRAGON_NAME_PREFIX_LENGTH,
                DRAGON_COLORS,
                DRAGON_GENDERS,
                DRAGON_ADDRESS_LENGTH);

        Thread.sleep(4000);

        new ElasticDataPopulator(
                dataClient,
                DATA_INDEX_NAME,
                DATA_TYPE_NAME,
                "id",
                () -> dragonsList
        ).populate();

        Thread.sleep(2000);
        StatCalculator.loadDefaultStatParameters(STAT_INDEX_NAME, STAT_TYPE_NUMERIC_NAME, STAT_TYPE_STRING_NAME, STAT_TYPE_TERM_NAME);
        StatCalculator.buildStatisticsBasedOnConfiguration(logger, dataClient, statClient, buildStatContainer() );
        Thread.sleep(3000);

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


    private static StatContainer buildStatContainer() {
        HistogramNumeric histogramDragonAge = HistogramNumeric.Builder.aHistogramNumeric()
                .withMin(DRAGON_MIN_AGE).withMax(DRAGON_MAX_AGE).withNumOfBins(10).build();

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
                .withDataType(DataType.string).withTerms(DRAGON_GENDERS)
                .build();

        HistogramTerm histogramDocType = HistogramTerm.Builder.aHistogramTerm()
                .withDataType(DataType.string).withBuckets(Collections.singletonList(
                        new BucketTerm(DATA_TYPE_NAME) // "Dragon"
                )).build();


        Field nameField = new Field(DATA_FIELD_NAME_NAME, histogramDragonName);
        Field ageField = new Field(DATA_FIELD_NAME_AGE, histogramDragonAge);
        Field addressField = new Field(DATA_FIELD_NAME_ADDRESS, histogramDragonAddress);
        Field colorField = new Field(DATA_FIELD_NAME_COLOR, histogramDragonColor);
        Field genderField = new Field(DATA_FIELD_NAME_GENDER, histogramTerm);
        Field dragonTypeField = new Field(DATA_FIELD_NAME_TYPE, histogramDocType);


        Type typeDragon = new Type(DATA_TYPE_NAME, Arrays.asList(ageField, nameField, addressField, colorField, genderField, dragonTypeField));

        Mapping mapping = Mapping.MappingBuilder.aMapping().withIndices(Arrays.asList(DATA_INDEX_NAME))
                .withTypes(Collections.singletonList(DATA_TYPE_NAME)).build();

        return StatContainer.Builder.aStatContainer()
                .withMappings(Collections.singletonList(mapping))
                .withTypes(Collections.singletonList(typeDragon))
                .build();
    }
}