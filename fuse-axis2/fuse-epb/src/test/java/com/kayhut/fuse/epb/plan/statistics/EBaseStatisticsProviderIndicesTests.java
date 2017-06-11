package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartition;
import com.kayhut.fuse.unipop.structure.ElementType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.kayhut.fuse.model.OntologyTestUtils.END_DATE;
import static com.kayhut.fuse.model.OntologyTestUtils.OWN;
import static com.kayhut.fuse.model.OntologyTestUtils.START_DATE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 08/05/2017.
 */
public class EBaseStatisticsProviderIndicesTests {
    GraphElementSchemaProvider graphElementSchemaProvider;
    GraphStatisticsProvider graphStatisticsProvider;
    Ontology.Accessor ont;
    EBaseStatisticsProvider statisticsProvider;
    PhysicalIndexProvider indexProvider;
    private static String INDEX_PREFIX = "idx-";
    private static String INDEX_FORMAT = "idx-%s";
    private static String DATE_FORMAT_STRING = "yyyy-MM-dd-HH";
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
    private long nowTime;
    @Before
    public void setUp() throws Exception {
        List<Statistics.BucketInfo<Date>> secondDateBuckets = new ArrayList<>();
        nowTime  = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").parse("2017-01-01-10-30-00").getTime();
        secondDateBuckets.add(new Statistics.BucketInfo<>(500L, 10L,new Date(nowTime -1000*60),new Date(nowTime +1000*60)));
        List<Statistics.BucketInfo<Date>> firstDateBuckets = new ArrayList<>();
        firstDateBuckets.add(new Statistics.BucketInfo<>(10L, 5L,new Date(nowTime -2*1000*60*60),new Date(nowTime -2*1000*59*60)));

        List<Statistics.BucketInfo<Long>> longBuckets = new ArrayList<>();
        longBuckets.add(new Statistics.BucketInfo<>(100L,10L, 0L, 100L ));
        longBuckets.add(new Statistics.BucketInfo<>(50L,10L, 100L, 200L ));

        List<Statistics.BucketInfo<Double>> doubleBuckets = new ArrayList<>();
        doubleBuckets.add(new Statistics.BucketInfo<>(100L,10L, 0d, 100d ));
        doubleBuckets.add(new Statistics.BucketInfo<>(50L,10L, 100d, 200d ));

        List<Statistics.BucketInfo<String>> firstStringBuckets = new ArrayList<>();
        List<Statistics.BucketInfo<String>> secondStringBuckets = new ArrayList<>();
        firstStringBuckets.add(new Statistics.BucketInfo<>(100L,10L, "a", "z"));
        secondStringBuckets.add(new Statistics.BucketInfo<>(50L,10L, "a", "z"));

        indexProvider = Mockito.mock(PhysicalIndexProvider.class);
        List<String> indices = Arrays.asList(String.format(INDEX_FORMAT,DATE_FORMAT.format(new Date(nowTime - 60*60*1000))),
                                             String.format(INDEX_FORMAT,DATE_FORMAT.format(new Date(nowTime))));

        when(indexProvider.getIndexPartitionByLabel(any(), eq(ElementType.edge)))
                .thenReturn(new TimeSeriesIndexPartition() {
            @Override
            public String getDateFormat() {
                return DATE_FORMAT_STRING;
            }

            @Override
            public String getIndexPrefix() {
                return INDEX_PREFIX;
            }

            @Override
            public String getIndexFormat() {
                return INDEX_FORMAT;
            }

            @Override
            public String getTimeField() {
                return "startDate";
            }

            @Override
            public String getIndexName(Date date) {
                return String.format(INDEX_FORMAT, DATE_FORMAT.format(date));
            }

            @Override
            public Iterable<String> getIndices() {
                return indices;
            }
        });


        ont = new Ontology.Accessor(OntologyTestUtils.createDragonsOntologyShort());
        RelationshipType relation2 = ont.$relation$(OWN.getrType());
        relation2.addProperty(1);

        graphElementSchemaProvider = new OntologySchemaProvider(ont.get(), indexProvider);
        graphStatisticsProvider = Mockito.mock(GraphStatisticsProvider.class);

        when(graphStatisticsProvider.getVertexCardinality(any())).thenReturn(new Statistics.Cardinality(1L, 1L));
        when(graphStatisticsProvider.getEdgeCardinality(any())).thenReturn(new Statistics.Cardinality(1L, 1L));
        when(graphStatisticsProvider.getEdgeCardinality(any(),any())).thenReturn(new Statistics.Cardinality(1000L, 1000L));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphEdgeSchema.class),any(),any(),any(),eq(Date.class))).thenAnswer(invocationOnMock -> {
            List<String> providedIndices = (List<String>) invocationOnMock.getArgumentAt(1, List.class );
            List<Statistics.BucketInfo<Date>> buckets = new ArrayList<>();
            if(providedIndices.contains(indices.get(0))){
                buckets.addAll(firstDateBuckets);
            }

            if(providedIndices.contains(indices.get(1))){
                buckets.addAll(secondDateBuckets);
            }
            return new Statistics.HistogramStatistics<>(buckets);
        });

        when(graphStatisticsProvider.getConditionHistogram(any(GraphEdgeSchema.class),any(),any(),any(),eq(String.class))).thenAnswer(invocationOnMock -> {
            List<String> providedIndices = (List<String>) invocationOnMock.getArgumentAt(1, List.class );

            Statistics.BucketInfo<String> bucket = null;
            if(providedIndices.contains(indices.get(0))){
                bucket = firstStringBuckets.get(0);
            }
            if(providedIndices.contains(indices.get(1))) {
                if(bucket == null)
                    bucket = secondStringBuckets.get(0);
                else {

                    Statistics.BucketInfo<String> stringBucketInfo = secondStringBuckets.get(0);
                    bucket = new Statistics.BucketInfo<>(bucket.getTotal() +stringBucketInfo.getTotal(), bucket.getCardinality()
                            + stringBucketInfo.getCardinality(), bucket.getLowerBound(), bucket.getHigherBound());
                }
            }


            List<Statistics.BucketInfo<String>> buckets = Arrays.asList(bucket);
            return new Statistics.HistogramStatistics<>(buckets);
        });

        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(Long.class))).thenReturn(new Statistics.HistogramStatistics<>(longBuckets));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(Date.class))).thenReturn(new Statistics.HistogramStatistics<>(secondDateBuckets));
        //when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),isA(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        statisticsProvider = new EBaseStatisticsProvider(graphElementSchemaProvider, ont, graphStatisticsProvider);
    }

    @Test
    public void eRelDateGtFilterSingleIndexHistogramTest() {
        Rel rel = new Rel();
        rel.setrType(OWN.getrType());

        RelProp prop = new RelProp();
        prop.setpType(Integer.toString(START_DATE.type));
        Constraint constraint = new Constraint();
        constraint.setExpr(new Date(nowTime));
        constraint.setOp(ConstraintOp.gt);
        prop.setCon(constraint);
        RelPropGroup relFilter = new RelPropGroup(Collections.singletonList(prop));

        Statistics.Cardinality nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(250, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eRelDateRangeFilterSingleIndexHistogramTest() {
        Rel rel = new Rel();
        rel.setrType(OWN.getrType());

        RelProp prop = new RelProp();
        prop.setpType(Integer.toString(START_DATE.type));
        Constraint constraint = new Constraint();
        constraint.setExpr(Arrays.asList(new Date(nowTime-1000),new Date(nowTime)));
        constraint.setOp(ConstraintOp.inRange);
        constraint.setiType("[]");
        prop.setCon(constraint);
        RelPropGroup relFilter = new RelPropGroup(Collections.singletonList(prop));

        Statistics.Cardinality nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(500/120d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(10/120d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eRelDateInSetFilterSingleIndexHistogramTest() {
        Rel rel = new Rel();
        rel.setrType(OWN.getrType());

        RelProp prop = new RelProp();
        prop.setpType(Integer.toString(START_DATE.type));
        Constraint constraint = new Constraint();
        constraint.setExpr(Arrays.asList(new Date(nowTime-1000),new Date(nowTime)));
        constraint.setOp(ConstraintOp.inSet);
        prop.setCon(constraint);
        RelPropGroup relFilter = new RelPropGroup(Collections.singletonList(prop));

        Statistics.Cardinality nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(100d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(2d, nodeStatistics.getCardinality(), 0.1);
    }


    @Test
    public void eRelDateNotInSetFilterSingleIndexHistogramTest() {
        Rel rel = new Rel();
        rel.setrType(OWN.getrType());

        RelProp prop = new RelProp();
        prop.setpType(Integer.toString(START_DATE.type));
        Constraint constraint = new Constraint();
        constraint.setExpr(Arrays.asList(new Date(nowTime-1000),new Date(nowTime)));
        constraint.setOp(ConstraintOp.notInSet);
        prop.setCon(constraint);
        RelPropGroup relFilter = new RelPropGroup(Collections.singletonList(prop));

        Statistics.Cardinality nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(500d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(10, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eRelStringGtFilterHistogramTest() {
        Rel rel = new Rel();
        rel.setrType(OWN.getrType());

        RelProp prop = new RelProp();
        prop.setpType("1");
        Constraint constraint = new Constraint();
        constraint.setExpr("abc");
        constraint.setOp(ConstraintOp.gt);
        prop.setCon(constraint);
        RelPropGroup relFilter = new RelPropGroup(Collections.singletonList(prop));

        Statistics.Cardinality nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(150, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eRelStringGtFilterWithDateFilterHistogramTest() {
        Rel rel = new Rel();
        rel.setrType(OWN.getrType());

        RelProp dateProp = new RelProp();
        dateProp.setpType(Integer.toString(START_DATE.type));
        Constraint constraint = new Constraint();
        constraint.setExpr(new Date(nowTime));
        constraint.setOp(ConstraintOp.gt);
        dateProp.setCon(constraint);
        RelProp stringProp = new RelProp();
        stringProp.setpType("1");
        constraint = new Constraint();
        constraint.setExpr("abc");
        constraint.setOp(ConstraintOp.gt);
        stringProp.setCon(constraint);
        RelPropGroup relFilter = new RelPropGroup(Arrays.asList(dateProp,stringProp));

        Statistics.Cardinality nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(50, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eRelStringGtFilterWithDateRangeFilterHistogramTest() {
        Rel rel = new Rel();
        rel.setrType(OWN.getrType());

        RelProp dateProp = new RelProp();
        dateProp.setpType(Integer.toString(START_DATE.type));
        Constraint constraint = new Constraint();
        constraint.setExpr(Arrays.asList(new Date(nowTime-60*1000),new Date(nowTime+60*1000)));
        constraint.setOp(ConstraintOp.inRange);
        constraint.setiType("[]");
        dateProp.setCon(constraint);
        RelProp stringProp = new RelProp();
        stringProp.setpType("1");
        constraint = new Constraint();
        constraint.setExpr("abc");
        constraint.setOp(ConstraintOp.gt);
        stringProp.setCon(constraint);
        RelPropGroup relFilter = new RelPropGroup(Arrays.asList(dateProp,stringProp));

        Statistics.Cardinality nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(50, nodeStatistics.getTotal(), 0.1);
    }
}
