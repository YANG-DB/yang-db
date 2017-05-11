package com.kayhut.fuse.epb.plan.statistics;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.execution.plan.Direction;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 * Created by liorp on 4/27/2017.
 */
public class EBaseStatisticsProviderBasicTests {
    GraphElementSchemaProvider graphElementSchemaProvider;
    GraphStatisticsProvider graphStatisticsProvider;
    Ontology ontology;
    EBaseStatisticsProvider statisticsProvider;
    PhysicalIndexProvider indexProvider;

    @Before
    public void setUp() throws Exception {
        List<Statistics.BucketInfo<Date>> dateBuckets = new ArrayList<>();
        long now = System.currentTimeMillis();
        dateBuckets.add(new Statistics.BucketInfo<>(10l,5l,new Date(now -2*1000*60*60),new Date(now -1000*60*60)));
        dateBuckets.add(new Statistics.BucketInfo<>(50l,10l,new Date(now -1000*60*60),new Date(now +1000*60*60)));

        List<Statistics.BucketInfo<Long>> longBuckets = new ArrayList<>();
        longBuckets.add(new Statistics.BucketInfo<>(100L,10L, 0L, 100L ));
        longBuckets.add(new Statistics.BucketInfo<>(50L,10L, 100L, 200L ));

        List<Statistics.BucketInfo<Double>> doubleBuckets = new ArrayList<>();
        doubleBuckets.add(new Statistics.BucketInfo<>(100L,10L, 0d, 100d ));
        doubleBuckets.add(new Statistics.BucketInfo<>(50L,10L, 100d, 200d ));

        List<Statistics.BucketInfo<String>> stringBuckets = new ArrayList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,10L, "a", "m"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,10L, "m", "z"));

        indexProvider = Mockito.mock(PhysicalIndexProvider.class);
        when(indexProvider.getIndexPartitionByLabel(any(), any())).thenReturn(() -> new LinkedList<>());

        ontology = OntologyTestUtils.createDragonsOntologyShort();
        graphElementSchemaProvider = new OntologySchemaProvider(indexProvider, ontology);
        graphStatisticsProvider = Mockito.mock(GraphStatisticsProvider.class);

        when(graphStatisticsProvider.getVertexCardinality(any())).thenReturn(new Statistics.Cardinality(1l, 1l));
        when(graphStatisticsProvider.getVertexCardinality(any(), any())).thenReturn(new Statistics.Cardinality(1000l, 1000l));
        when(graphStatisticsProvider.getEdgeCardinality(any())).thenReturn(new Statistics.Cardinality(1l, 1l));
        when(graphStatisticsProvider.getEdgeCardinality(any(),any())).thenReturn(new Statistics.Cardinality(1000l, 1000l));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphEdgeSchema.class),any(),any(),any(),isA(Date.class))).thenReturn(new Statistics.HistogramStatistics<>(dateBuckets));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),isA(Long.class))).thenReturn(new Statistics.HistogramStatistics<>(longBuckets));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),isA(Date.class))).thenReturn(new Statistics.HistogramStatistics<>(dateBuckets));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),isA(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        statisticsProvider = new EBaseStatisticsProvider(graphElementSchemaProvider, ontology, graphStatisticsProvider);
    }


    @Test
    public void eConcreteHistogramTest() {
        EConcrete eConcrete = new EConcrete();
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeStatistics(eConcrete);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(1d, nodeStatistics.getTotal(), 0);
    }

    @Test
    public void eRelHistogramTest() {
        Rel rel = new Rel();
        rel.setrType(2);
        Statistics.Cardinality nodeStatistics = statisticsProvider.getEdgeStatistics(rel);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(1d, nodeStatistics.getTotal(), 0);
    }

    @Test
    public void eRelDateEqFilterHistogramTest() {
        Rel rel = new Rel();
        rel.setrType(2);
        RelPropGroup relFilter = new RelPropGroup();

        RelProp prop = new RelProp();
        prop.setpType("8");
        Constraint constraint = new Constraint();
        constraint.setExpr(new Date());
        constraint.setOp(ConstraintOp.eq);
        prop.setCon(constraint);
        relFilter.setrProps(Collections.singletonList(prop));

        Statistics.Cardinality nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(5, nodeStatistics.getTotal(), 0.1);
    }


    @Test
    public void eRelDateGtFilterHistogramTest() {
        Rel rel = new Rel();
        rel.setrType(2);
        RelPropGroup relFilter = new RelPropGroup();

        RelProp prop = new RelProp();
        prop.setpType("8");
        Constraint constraint = new Constraint();
        constraint.setExpr(new Date());
        constraint.setOp(ConstraintOp.gt);
        prop.setCon(constraint);
        relFilter.setrProps(Collections.singletonList(prop));

        Statistics.Cardinality nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(25, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eRelDateGeFilterHistogramTest() {
        Rel rel = new Rel();
        rel.setrType(2);
        RelPropGroup relFilter = new RelPropGroup();

        RelProp prop = new RelProp();
        prop.setpType("8");
        Constraint constraint = new Constraint();
        constraint.setExpr(new Date());
        constraint.setOp(ConstraintOp.ge);
        prop.setCon(constraint);
        relFilter.setrProps(Collections.singletonList(prop));

        Statistics.Cardinality nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(25, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eRelDateLtFilterHistogramTest() {
        Rel rel = new Rel();
        rel.setrType(2);
        RelPropGroup relFilter = new RelPropGroup();

        RelProp prop = new RelProp();
        prop.setpType("8");
        Constraint constraint = new Constraint();
        constraint.setExpr(new Date());
        constraint.setOp(ConstraintOp.lt);
        prop.setCon(constraint);
        relFilter.setrProps(Collections.singletonList(prop));

        Statistics.Cardinality nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(35, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eRelDateLeFilterHistogramTest() {
        Rel rel = new Rel();
        rel.setrType(2);
        RelPropGroup relFilter = new RelPropGroup();

        RelProp prop = new RelProp();
        prop.setpType("8");
        Constraint constraint = new Constraint();
        constraint.setExpr(new Date());
        constraint.setOp(ConstraintOp.le);
        prop.setCon(constraint);
        relFilter.setrProps(Collections.singletonList(prop));

        Statistics.Cardinality nodeStatistics = statisticsProvider.getEdgeFilterStatistics(rel, relFilter);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(35, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eTypedHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeStatistics(eTyped);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(1d, nodeStatistics.getTotal(), 0);
    }

    @Test
    public void eTypedDateFilterEqHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("4");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        con.setExpr(new Date());
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);

        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(5d, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eTypedStringFilterEqHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        con.setExpr("abc");
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);

        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(10d, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eTypedStringFilterGeHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.ge);
        con.setExpr("abc");
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);

        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(150d, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eTypedStringFilterGe2HistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.ge);
        con.setExpr("m");
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);

        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(50d, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eTypedCompositeFilterTest(){
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("4");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        con.setExpr(new Date());
        prop.setCon(con);
        props.add(prop);
        prop = new EProp();
        prop.setpType("6");
        con = new Constraint();
        con.setOp(ConstraintOp.eq);
        con.setExpr(10L);
        prop.setCon(con);
        props.add(prop);

        propGroup.seteProps(props);

        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(5d, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eTypedLongFilterEqHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("6");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        con.setExpr(10L);
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);

        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(10d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedLongFilterNeHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("6");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.ne);
        con.setExpr(10L);
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);

        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(150d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(20d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedLongFilterGtHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("6");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.gt);
        con.setExpr(10L);
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);

        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(140d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(19d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedLongFilterGeHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("6");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.ge);
        con.setExpr(10L);
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);

        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(140d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(19d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedLongFilterLtHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("6");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.lt);
        con.setExpr(10L);
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);

        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(10d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedLongFilterLeHistogramTest() {
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("6");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.le);
        con.setExpr(10L);
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);

        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(10d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eUnTypedHistogramTest() {
        EUntyped eUntyped = new EUntyped();
        eUntyped.setvTypes(Arrays.asList(1,2));
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeStatistics(eUntyped);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(2d, nodeStatistics.getTotal(), 0);
    }

    @Test
    public void eUnTypedDateFilterEqHistogramTest() {
        EUntyped eUntyped = new EUntyped();
        eUntyped.setvTypes(Arrays.asList(1));
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("4");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        con.setExpr(new Date());
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);

        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eUntyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(5d, nodeStatistics.getTotal(), 0.1);
    }

    @Test
    public void eTypedEnumeratedFieldTest(){
        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("3");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        Value value = new Value();
        value.setName("male");
        value.setVal(2);
        con.setExpr(value);
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);

        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(5d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedEnumeratedSingleValueBucketFieldTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,1L, "female", "female"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "male", "male"));
        when(graphStatisticsProvider.<String>getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),isA(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("3");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        Value value = new Value();
        value.setName("male");
        value.setVal(2);
        con.setExpr(value);
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);

        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
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
        when(graphStatisticsProvider.<String>getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),isA(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.ge);
        con.setExpr("edf");
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
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
        when(graphStatisticsProvider.<String>getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),isA(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.le);
        con.setExpr("edf");
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
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
        when(graphStatisticsProvider.<String>getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),isA(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.lt);
        con.setExpr("edf");
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(100d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);

    }

    @Test
    public void eTypedStringGtSingleValueBucketsTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,1L, "abc", "abc"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "edf", "edf"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "egh", "egh"));
        when(graphStatisticsProvider.<String>getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),isA(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.gt);
        con.setExpr("edf");
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
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
        when(graphStatisticsProvider.<String>getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),isA(List.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.inRange);
        con.setExpr(Arrays.asList("f", "r"));
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
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
        when(graphStatisticsProvider.<String>getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),isA(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.startsWith);
        con.setExpr("g");
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
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
        when(graphStatisticsProvider.<String>getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),isA(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.startsWith);
        con.setExpr("f");
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
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
        when(graphStatisticsProvider.<String>getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),isA(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.startsWith);
        con.setExpr("a");
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
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
        when(graphStatisticsProvider.<String>getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),isA(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.startsWith);
        con.setExpr("e");
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(100d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(20d, nodeStatistics.getCardinality(), 0.1);
        con.setExpr("ed");
        nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(50d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(10d, nodeStatistics.getCardinality(), 0.1);
    }

    @Test
    public void eTypedStringStartsSingleValueBucketsExactTest(){
        List<Statistics.BucketInfo<String>> stringBuckets = new LinkedList<>();
        stringBuckets.add(new Statistics.BucketInfo<>(100L,1L, "abc", "abc"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "edf", "edf"));
        stringBuckets.add(new Statistics.BucketInfo<>(50L,1L, "egh", "egh"));
        when(graphStatisticsProvider.getConditionHistogram(any(GraphVertexSchema.class),any(),any(),any(),isA(String.class))).thenReturn(new Statistics.HistogramStatistics<>(stringBuckets));

        ETyped eTyped = new ETyped();
        eTyped.seteType(1);
        EPropGroup propGroup = new EPropGroup();
        ArrayList<EProp> props = new ArrayList<>();
        EProp prop = new EProp();
        prop.setpType("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.startsWith);
        con.setExpr("edf");
        prop.setCon(con);
        props.add(prop);
        propGroup.seteProps(props);
        Statistics.Cardinality nodeStatistics = statisticsProvider.getNodeFilterStatistics(eTyped,propGroup);
        Assert.assertNotNull(nodeStatistics);
        Assert.assertEquals(50d, nodeStatistics.getTotal(), 0.1);
        Assert.assertEquals(1d, nodeStatistics.getCardinality(), 0.1);
    }
}
