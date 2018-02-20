package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Value;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by liorp on 4/27/2017.
 */
public class EBaseStatisticsProviderBasicTests {
    @Before
    public void setUp() throws Exception {
        List<Statistics.BucketInfo<Date>> dateBuckets = new ArrayList<>();
        long now = System.currentTimeMillis();
        dateBuckets.add(new Statistics.BucketInfo<>(10L, 5L,new Date(now -2*1000*60*60),new Date(now -1000*60*60)));
        dateBuckets.add(new Statistics.BucketInfo<>(50L, 10L,new Date(now -1000*60*60),new Date(now +1000*60*60)));

        List<Statistics.BucketInfo<Long>> longBuckets = new ArrayList<>();
        longBuckets.add(new Statistics.BucketInfo<>(100L,10L, 0L, 100L ));
        longBuckets.add(new Statistics.BucketInfo<>(50L,10L, 100L, 200L ));

        List<Statistics.BucketInfo<Double>> doubleBuckets = new ArrayList<>();
        doubleBuckets.add(new Statistics.BucketInfo<>(100L,10L, 0d, 100d ));
        doubleBuckets.add(new Statistics.BucketInfo<>(50L,10L, 100d, 200d ));

        List<Statistics.BucketInfo<String>> stringBuckets = new ArrayList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,10L, "a", "m"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,10L, "m", "z"));

        ontology = OntologyTestUtils.createDragonsOntologyShort();
        graphElementSchemaProvider = buildSchemaProvider(new Ontology.Accessor(ontology));
        graphStatisticsProvider = Mockito.mock(GraphStatisticsProvider.class);

        when(graphStatisticsProvider.getVertexCardinality(any())).thenReturn(new Statistics.SummaryStatistics(1L, 1L));
        when(graphStatisticsProvider.getVertexCardinality(any(), any())).thenReturn(new Statistics.SummaryStatistics(1000L, 1000L));
        when(graphStatisticsProvider.getEdgeCardinality(any())).thenReturn(new Statistics.SummaryStatistics(1L, 1L));
        when(graphStatisticsProvider.getEdgeCardinality(any(),any())).thenReturn(new Statistics.SummaryStatistics(1000L, 1000L));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphEdgeSchema.class),any(),any(),any(),eq(Date.class))).thenReturn(new Statistics.HistogramStatistics<>(dateBuckets));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(Long.class))).thenReturn(new Statistics.HistogramStatistics<>(longBuckets));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(Date.class))).thenReturn(new Statistics.HistogramStatistics<>(dateBuckets));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        statisticsProvider = new EBaseStatisticsProvider(graphElementSchemaProvider, new Ontology.Accessor(ontology), graphStatisticsProvider);
    }


    @Test
    public void eConcreteHistogramTest() {
        EConcrete eConcrete = new EConcrete();
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeStatistics(eConcrete);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(1d, nodeStatistics.getTotal(), 0);
    }

    @Test
    public void eRelHistogramTest() {
        Rel rel = new Rel(0, OWN.getrType(), Rel.Direction.L, null, 0,0);
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getEdgeStatistics(rel);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(1d, nodeStatistics.getTotal(), 0);
    }

    @Test
    public void eRelDateEqFilterHistogramTest() {
        Rel rel = new Rel(0,REGISTERED.getrType(), Rel.Direction.L, null, 0, 0);
        RelProp prop = RelProp.of(START_DATE.type, 0, Constraint.of(ConstraintOp.eq, new Date()));
        RelPropGroup relFilter = new RelPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(5, nodeStatistics.getTotal(), 0.1);
    }


    @Test
    public void eRelDateGtFilterHistogramTest() {
        Rel rel = new Rel(0, SUBJECT.getrType(), Rel.Direction.L, null, 0, 0);
        RelProp prop = RelProp.of(END_DATE.type, 0, Constraint.of(ConstraintOp.gt, new Date()));
        RelPropGroup relFilter = new RelPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(25, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eRelDateGeFilterHistogramTest() {
        Rel rel = new Rel(0,FREEZE.getrType(), Rel.Direction.L, null, 0,0);

        RelProp prop = RelProp.of(END_DATE.type, 0, Constraint.of(ConstraintOp.ge, new Date()));
        RelPropGroup relFilter = new RelPropGroup(Collections.singletonList(prop));

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(25, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eRelDateLtFilterHistogramTest() {
        Rel rel = new Rel(0,FIRE.getrType(), Rel.Direction.L, null,0,0);

        RelProp prop = RelProp.of(START_DATE.type, 0, Constraint.of(ConstraintOp.lt, new Date()));
        RelPropGroup relFilter = new RelPropGroup(Collections.singletonList(prop));

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(35, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eRelDateLeFilterHistogramTest() {
        Rel rel = new Rel(0,OWN.getrType(), Rel.Direction.L, null,0,0);

        RelProp prop = RelProp.of(END_DATE.type, 0, Constraint.of(ConstraintOp.le, new Date()));
        RelPropGroup relFilter = new RelPropGroup(Collections.singletonList(prop));

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(35, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eTypedHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeStatistics(eTyped);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(1d, nodeStatistics.getTotal(), 0);
    }

    @Test
    public void eTypedDateFilterEqHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        ArrayList<EProp> props = new ArrayList<>();
        props.add(EProp.of("birthDate", 0, Constraint.of(ConstraintOp.eq, new Date())));
        EPropGroup propGroup = new EPropGroup(props);

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(5d, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eTypedStringFilterEqHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        ArrayList<EProp> props = new ArrayList<>();
        props.add(EProp.of("firstName", 0, Constraint.of(ConstraintOp.eq, "abc")));
        EPropGroup propGroup = new EPropGroup(props);

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(10d, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eTypedStringFilterGeHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        ArrayList<EProp> props = new ArrayList<>();
        props.add(EProp.of("firstName", 0, Constraint.of(ConstraintOp.ge, "abc")));
        EPropGroup propGroup = new EPropGroup(props);

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(150d, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eTypedStringFilterGe2HistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        ArrayList<EProp> props = new ArrayList<>();
        props.add(EProp.of("firstName", 0, Constraint.of(ConstraintOp.ge, "m")));
        EPropGroup propGroup = new EPropGroup(props);

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(50d, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eTypedCompositeFilterTest(){
        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        ArrayList<EProp> props = new ArrayList<>();
        props.add(EProp.of("birthDate", 0, Constraint.of(ConstraintOp.eq, new Date())));
        props.add(EProp.of("height", 0, Constraint.of(ConstraintOp.eq, 10L)));

        EPropGroup propGroup = new EPropGroup(props);

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(5d, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eTypedLongFilterEqHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("height", 0, Constraint.of(ConstraintOp.eq, 10L));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(10d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedLongFilterNeHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("height", 0, Constraint.of(ConstraintOp.ne, 10L));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(150d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(20d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedLongFilterGtHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("height", 0, Constraint.of(ConstraintOp.gt, 10L));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(140d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(19d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedLongFilterGeHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("height", 0, Constraint.of(ConstraintOp.ge, 10L));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(140d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(19d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedLongFilterLtHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("height", 0, Constraint.of(ConstraintOp.lt, 10L));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(10d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedLongFilterLeHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("height", 0, Constraint.of(ConstraintOp.le , 10L));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(10d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eUnTypedHistogramTest() {
        EUntyped eUntyped = new EUntyped();
        eUntyped.setvTypes(Arrays.asList("Person","Dragon"));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeStatistics(eUntyped);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(2d, nodeStatistics.getTotal(), 0);
    }

    @Test
    public void eUnTypedDateFilterEqHistogramTest() {
        EUntyped eUntyped = new EUntyped();
        eUntyped.setvTypes(Collections.singletonList("Person"));
        EProp prop = EProp.of("birthDate", 0, Constraint.of(ConstraintOp.eq, new Date()));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eUntyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(5d, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    @Ignore
    public void eTypedEnumeratedFieldTest(){
        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("3", 0, Constraint.of(ConstraintOp.eq, Value.ValueBuilder.aValue().withName("male").withVal(2).build()));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(5d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    @Ignore
    public void eTypedEnumeratedSingleValueBucketFieldTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,1L, "female", "female"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "male", "male"));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop =  EProp.of("3", 0, Constraint.of(ConstraintOp.eq, Value.ValueBuilder.aValue().withName("male").withVal(2).build()));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));

        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(50d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedStringGeSingleValueBucketsTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,1L, "abc", "abc"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "edf", "edf"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "egh", "egh"));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("firstName", 0, Constraint.of(ConstraintOp.ge, "edf"));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(100d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(2d, nodeStatistics.getCardinality(), 0.1);

    }

    @Test
    public void eTypedStringLeSingleValueBucketsTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,1L, "abc", "abc"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "edf", "edf"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "egh", "egh"));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("firstName", 0, Constraint.of(ConstraintOp.le, "edf"));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(150d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(2d, nodeStatistics.getCardinality(), 0.1);

    }

    @Test
    public void eTypedStringLtSingleValueBucketsTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,1L, "abc", "abc"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "edf", "edf"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "egh", "egh"));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("firstName", 0, Constraint.of(ConstraintOp.lt, "edf"));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(100d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);

    }

    @Test
    public void eTypedStringNeSingleValueBucketsTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,1L, "abc", "abc"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "edf", "edf"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "egh", "egh"));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("firstName", 0, Constraint.of(ConstraintOp.ne, "edf"));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(150d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(2d, nodeStatistics.getCardinality(), 0.1);

    }

    @Test
    public void eTypedStringNotInSetSingleValueBucketsTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,1L, "abc", "abc"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "edf", "edf"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "egh", "egh"));
        when(graphStatisticsProvider.<String>getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("firstName", 0, Constraint.of(ConstraintOp.notInSet, Arrays.asList("edf", "abc")));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(50d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);

    }


    @Test
    public void eTypedStringGtSingleValueBucketsTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,1L, "abc", "abc"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "edf", "edf"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "egh", "egh"));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("firstName", 0, Constraint.of(ConstraintOp.gt, "edf"));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(50d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);
    }


    @Test
    public void eTypedStringRangeTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,10L, "a", "f"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,10L, "f", "ra"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,10L, "ra", "z"));
        when(graphStatisticsProvider.<String>getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("firstName", 0, Constraint.of(ConstraintOp.inRange, Arrays.asList("f","r"), "[]"));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(50d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(10d, nodeStatistics.getCardinality(), 0.1);

    }

    @Test
    public void eTypedStringRangeExclusiveTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,10L, "a", "f"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,10L, "f", "r"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,10L, "ra", "z"));
        when(graphStatisticsProvider.<String>getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("firstName", 0, Constraint.of(ConstraintOp.inRange, Arrays.asList("f","ra"), "[)"));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(50d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(10d, nodeStatistics.getCardinality(), 0.1);

    }

    @Test
    public void eTypedStringStartsWithTwoBucketsTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,10L, "a", "f"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,10L, "f", "ga"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,10L, "ga", "z"));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("firstName", 0, Constraint.of(ConstraintOp.startsWith, "g"));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(100d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(20d, nodeStatistics.getCardinality(), 0.1);

    }

    @Test
    public void eTypedStringStartsWithSingleBucketsTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,1L, "a", "f"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "f", "ga"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "ga", "z"));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("firstName", 0, Constraint.of(ConstraintOp.startsWith, "f"));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(50d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedStringStartsWithNoBucketsTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,10L, "c", "f"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,10L, "f", "ga"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,10L, "ga", "z"));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("firstName", 0, Constraint.of(ConstraintOp.startsWith, "a"));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(0d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(0d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedStringStartsSingleValueBucketsTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,10L, "abc", "abc"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,10L, "edf", "edf"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,10L, "egh", "egh"));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("firstName", 0, Constraint.of(ConstraintOp.startsWith, "e"));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(100d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(20d, nodeStatistics.getCardinality(), 0.1);
        prop.getCon().setExpr("ed");
        nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(50d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(10d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedStringNotStartsSingleValueBucketsTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,10L, "abc", "abc"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,10L, "edf", "edf"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,10L, "egh", "egh"));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("firstName", 0, Constraint.of(ConstraintOp.notStartsWith, "e"));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(100d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(10d, nodeStatistics.getCardinality(), 0.1);
        prop.getCon().setExpr("ed");
        nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(150d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(20d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedStringStartsSingleValueBucketsExactTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,1L, "abc", "abc"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "edf", "edf"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "egh", "egh"));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),eq(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType("Person");
        EProp prop = EProp.of("firstName", 0, Constraint.of(ConstraintOp.startsWith, "edf"));
        EPropGroup propGroup = new EPropGroup(Collections.singletonList(prop));
        Statistics.SummaryStatistics nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(50d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);
    }

    //region Private Methods
    private GraphElementSchemaProvider buildSchemaProvider(Ontology.Accessor ont) {
        Iterable<GraphVertexSchema> vertexSchemas =
                Stream.ofAll(ont.entities())
                        .map(entity -> (GraphVertexSchema) new GraphVertexSchema.Impl(
                                entity.geteType(),
                                new StaticIndexPartitions(Collections.emptyList())))
                        .toJavaList();

        Iterable<GraphEdgeSchema> edgeSchemas =
                Stream.ofAll(ont.relations())
                        .map(relation -> (GraphEdgeSchema) new GraphEdgeSchema.Impl(
                                relation.getrType(),
                                new GraphElementConstraint.Impl(__.has(T.label, relation.getrType())),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(relation.getePairs().get(0).geteTypeA() + "IdA"),
                                        Optional.of(relation.getePairs().get(0).geteTypeA()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList(relation.getePairs().get(0).geteTypeB() + "IdB"),
                                        Optional.of(relation.getePairs().get(0).geteTypeB()))),
                                Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Collections.emptyList())),
                                Collections.emptyList()))
                        .toJavaList();

        return new OntologySchemaProvider(ont.get(), new GraphElementSchemaProvider.Impl(vertexSchemas, edgeSchemas));
    }
    //endregion

    //region Fields
    GraphElementSchemaProvider graphElementSchemaProvider;
    GraphStatisticsProvider graphStatisticsProvider;
    Ontology ontology;
    EBaseStatisticsProvider statisticsProvider;
    //endregion
}
