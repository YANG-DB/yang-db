package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.epb.plan.statistics.configuration.StatConfig;
import com.kayhut.fuse.epb.plan.statistics.provider.ElasticStatisticsGraphProvider;
import com.kayhut.fuse.epb.util.EpbUtil;
import com.kayhut.fuse.stat.configuration.StatConfiguration;
import com.kayhut.fuse.stat.es.client.ClientProvider;
import com.kayhut.fuse.stat.es.populator.ElasticDataPopulator;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.bucket.BucketTerm;
import com.kayhut.fuse.stat.model.configuration.Field;
import com.kayhut.fuse.stat.model.configuration.Mapping;
import com.kayhut.fuse.stat.model.configuration.StatContainer;
import com.kayhut.fuse.stat.model.configuration.Type;
import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.histogram.*;
import com.kayhut.test.framework.index.ElasticEmbeddedNode;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by benishue on 25-May-17.
 */
public class ElasticStatisticsGraphProviderTest {

    static TransportClient dataClient;
    static TransportClient statClient;
    static ElasticEmbeddedNode elasticEmbeddedNode;
    static StatConfig statConfig;
    static final int numOfDragonsInIndex1 = 1000;
    static final int numOfDragonsInIndex2 = 555; //HAMSA HAMSA HAMSA


    @Test
    public void getVertexCardinality() throws Exception {
        ElasticStatisticsGraphProvider statisticsGraphProvider = new ElasticStatisticsGraphProvider(statConfig);
    }

    @Test
    public void getVertexCardinality1() throws Exception {

    }

    @Test
    public void getEdgeCardinality() throws Exception {

    }

    @Test
    public void getEdgeCardinality1() throws Exception {

    }

    @Test
    public void getConditionHistogram() throws Exception {

    }

    @Test
    public void getConditionHistogram1() throws Exception {

    }

    @Test
    public void getGlobalSelectivity() throws Exception {

    }


    @BeforeClass
    public static void setup() throws Exception {

        statConfig = new StatConfig("fuse.test_elastic",
                Arrays.asList("localhost"),
                9300,
                "stat",
                "bucketTerm",
                "bucketString",
                "bucketNumeric",
                "count",
                "cardinality",
                buildStatContainer());

        dataClient = ClientProvider.getDataClient(null);
        statClient = ClientProvider.getDataClient(null);
        elasticEmbeddedNode = new ElasticEmbeddedNode();

        Thread.sleep(4000);

        new ElasticDataPopulator(
                dataClient,
                "index1",
                "dragon",
                "id",
                () -> EpbUtil.createDragons(numOfDragonsInIndex1)).populate();

        new ElasticDataPopulator(
                dataClient,
                "index2",
                "dragon",
                "id",
                () -> EpbUtil.createDragons(numOfDragonsInIndex2)).populate();

        Thread.sleep(2000);

    }

    //region Private Methods
    private static StatContainer buildStatContainer() {
        HistogramNumeric histogramDragonAge = HistogramNumeric.HistogramNumericBuilder.aHistogramNumeric()
                .withMin(10).withMax(100).withNumOfBins(10).build();

        HistogramString histogramDragonName = HistogramString.HistogramStringBuilder.aHistogramString()
                .withPrefixSize(3)
                .withInterval(10).withNumOfChars(26).withFirstCharCode("97").build();

        HistogramManual histogramDragonAddress = HistogramManual.HistogramManualBuilder.aHistogramManual()
                .withBuckets(Arrays.asList(
                        new BucketRange("abc", "dzz"),
                        new BucketRange("efg", "hij"),
                        new BucketRange("klm", "xyz")
                )).withDataType(DataType.string)
                .build();

        HistogramComposite histogramDragonColor = HistogramComposite.HistogramCompositeBuilder.aHistogramComposite()
                .withManualBuckets(Arrays.asList(
                        new BucketRange("00", "11"),
                        new BucketRange("22", "33"),
                        new BucketRange("44", "55")
                )).withDataType(DataType.string)
                .withAutoBuckets(HistogramString.HistogramStringBuilder.aHistogramString()
                        .withFirstCharCode("97")
                        .withInterval(10)
                        .withNumOfChars(26)
                        .withPrefixSize(3).build())
                .build();

        HistogramTerm histogramTerm = HistogramTerm.HistogramTermBuilder.aHistogramTerm()
                .withDataType(DataType.string).withBuckets(Arrays.asList(
                        new BucketTerm("male"),
                        new BucketTerm("female")
                )).build();

        HistogramTerm histogramDocType = HistogramTerm.HistogramTermBuilder.aHistogramTerm()
                .withDataType(DataType.string).withBuckets(Arrays.asList(
                        new BucketTerm("dragon")
                )).build();


        Type typeDragon = new Type("dragon", Arrays.asList(
                new Field("age", histogramDragonAge),
                new Field("name", histogramDragonName),
                new Field("address", histogramDragonAddress),
                new Field("color", histogramDragonColor),
                new Field("gender", histogramTerm),
                new Field("_type", histogramDocType)));

        Mapping mapping = Mapping.MappingBuilder.aMapping().withIndices(Arrays.asList("index1", "index2"))
                .withTypes(Collections.singletonList("dragon")).build();

        return StatContainer.StatContainerBuilder.aStatContainer()
                .withMappings(Collections.singletonList(mapping))
                .withTypes(Collections.singletonList(typeDragon))
                .build();
    }
    //endregion

}
