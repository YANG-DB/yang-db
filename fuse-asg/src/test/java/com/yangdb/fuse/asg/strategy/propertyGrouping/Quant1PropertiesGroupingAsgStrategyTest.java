package com.yangdb.fuse.asg.strategy.propertyGrouping;

import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.model.query.aggregation.AggLOp;
import com.yangdb.fuse.model.query.properties.CalculatedEProp;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.projection.CalculatedFieldProjection;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.eProp;
import static com.yangdb.fuse.model.query.properties.constraint.Constraint.*;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.*;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.gt;
import static com.yangdb.fuse.model.query.quant.QuantType.*;

public class Quant1PropertiesGroupingAsgStrategyTest {
    @Test
    public void test_quant_with_1_eProps() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(eProp(3, "p1", of(eq, "abc" )))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "p1", of(eq, "abc"))))
                .build();

        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void test_quant_with_2_eProps() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1"))
                .next(quant1(2, all))
                .in(
                        eProp(3, "p1", of(eq, "abc" )),
                        eProp(4, "p2", of(eq, 1)),
                        eProp(101, "p->eTag", new CalculatedFieldProjection(AggLOp.count)))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(ePropGroup(
                        3,
                        EProp.of(3, "p1", of(eq, "abc")),
                        EProp.of(4, "p2", of(eq, 1)),
                        CalculatedEProp.of(101, "p->eTag", new CalculatedFieldProjection(AggLOp.count))))
                .build();

        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void test_quant_with_2_ePropsGroup() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1"))
                .next(quant1(2, all))
                .in(
                        ePropGroup(
                                3,
                                EProp.of(3, "p1", of(eq, "abc")),
                                EProp.of(4, "p2", of(eq, 1))),
                        ePropGroup(
                                5,
                                EProp.of(5, "p1", of(eq, "abc")),
                                EProp.of(6, "p2", of(eq, 1))))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1"))
                .next(quant1(2, all))
                .in(ePropGroup(3,
                        EPropGroup.of(3,
                            EProp.of(3, "p1", of(eq, "abc")),
                            EProp.of(4, "p2", of(eq, 1)))
                        ,
                        EPropGroup.of(5,
                                EProp.of(5, "p1", of(eq, "abc")),
                                EProp.of(6, "p2", of(eq, 1))))
                )
                .build();

        Assert.assertEquals(AsgQueryDescriptor.print(expectedQuery), AsgQueryDescriptor.print(query));
    }

    @Test
    public void test_quant_with_3_eProps() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(
                        eProp(3, "p1", of(eq, "abc" )),
                        eProp(4, "p2", of(eq, 1)),
                        eProp(5, "p3", of(gt, 6)))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(ePropGroup(3,
                        EProp.of(3, "p1", of(eq, "abc")),
                        EProp.of(4, "p2", of(eq, 1)),
                        EProp.of(5, "p3", of(gt, 6))))
                .build();

        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void test_1_nested_quant_with_1_eProps() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .next(quant1(3, all)).in(eProp(4, "p1", of(eq, "abc" )))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(ePropGroup(4, EPropGroup.of(4, EProp.of(4, "p1", of(eq, "abc")))))
                .build();

        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void test_1_nested_quant_with_2_eProps() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .next(quant1(3, all)).in(
                        eProp(4, "p1", of(eq, "abc" )),
                        eProp(5, "p2", of(eq, 1 )))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(ePropGroup(4, EPropGroup.of(4,
                        EProp.of(4, "p1", of(eq, "abc")),
                        EProp.of(5, "p2", of(eq, 1)))))
                .build();

        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void test_1_nested_quant_with_3_eProps() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .next(quant1(3, all)).in(
                        eProp(4, "p1", of(eq, "abc" )),
                        eProp(5, "p2", of(eq, 1 )),
                        eProp(6, "p3", of(gt, 6)))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(ePropGroup(4, EPropGroup.of(4,
                        EProp.of(4, "p1", of(eq, "abc")),
                        EProp.of(5, "p2", of(eq, 1)),
                        EProp.of(6, "p3", of(gt, 6)))))
                .build();

        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void test_2_nested_quants_with_1_eProps() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                    .in(quant1(3, all).next(eProp(4, "p1", of(eq, "abc"))),
                        quant1(5, all).next(eProp(6, "p2", of(eq, 1))))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(ePropGroup(4,
                        EPropGroup.of(4, EProp.of(4, "p1", of(eq, "abc"))),
                        EPropGroup.of(6, EProp.of(6, "p2", of(eq, 1)))))
                .build();

        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void test_2_nested_quants_with_2_eProps() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(quant1(3, all)
                                .next(eProp(4, "p1", of(eq, "abc")))
                                .next(eProp(5, "p2", of(eq, 1))),
                    quant1(6, all)
                            .next(eProp(7, "p3", of(gt, 6)))
                            .next(eProp(8, "p4", of(inSet, Arrays.asList("s", "t")))))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(ePropGroup(4,
                        EPropGroup.of(4,
                                EProp.of(4, "p1", of(eq, "abc")),
                                EProp.of(5, "p2", of(eq, 1))),
                        EPropGroup.of(7,
                                EProp.of(7, "p3", of(gt, 6)),
                                EProp.of(8, "p4", of(inSet, Arrays.asList("s", "t"))))))
                .build();

        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void test_2_nested_quants_with_3_eProps() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(quant1(3, all)
                                .next(eProp(4, "p1", of(eq, "abc")))
                                .next(eProp(5, "p2", of(eq, 1)))
                                .next(eProp(6, "p3", of(gt, 6))),
                        quant1(7, all)
                                .next(eProp(8, "p4", of(inSet, Arrays.asList("s", "t"))))
                                .next(eProp(9, "p5", of(like, "*def*")))
                                .next(eProp(10, "p6", of(likeAny, Arrays.asList("s*", "*t")))))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(ePropGroup(4,
                        EPropGroup.of(4,
                                EProp.of(4, "p1", of(eq, "abc")),
                                EProp.of(5, "p2", of(eq, 1)),
                                EProp.of(6, "p3", of(gt, 6))),
                        EPropGroup.of(8,
                                EProp.of(8, "p4", of(inSet, Arrays.asList("s", "t"))),
                                EProp.of(9, "p5", of(like, "*def*")),
                                EProp.of(10, "p6", of(likeAny, Arrays.asList("s*", "*t"))))))
                .build();

        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void test_2_nested_all_some_quants_with_1_eProps() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(quant1(3, some).next(eProp(4, "p1", of(eq, "abc"))),
                        quant1(5, some).next(eProp(6, "p2", of(eq, 1))))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(ePropGroup(4,
                        EPropGroup.of(4, some, EProp.of(4, "p1", of(eq, "abc"))),
                        EPropGroup.of(6, some, EProp.of(6, "p2", of(eq, 1)))))
                .build();

        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void test_2_nested_some_all_quants_with_1_eProps() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, some))
                .in(quant1(3, all).next(eProp(4, "p1", of(eq, "abc"))),
                        quant1(5, all).next(eProp(6, "p2", of(eq, 1))))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1")).next(quant1(2, some))
                .in(ePropGroup(4, some,
                        EPropGroup.of(4, all, EProp.of(4, "p1", of(eq, "abc"))),
                        EPropGroup.of(6, all, EProp.of(6, "p2", of(eq, 1)))))
                .build();

        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void test_2_nested_all_some_quants_with_2_eProps() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(quant1(3, some)
                                .next(eProp(4, "p1", of(eq, "abc")))
                                .next(eProp(5, "p2", of(eq, 1))),
                        quant1(6, some)
                                .next(eProp(7, "p3", of(gt, 6)))
                                .next(eProp(8, "p4", of(inSet, Arrays.asList("s", "t")))))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(ePropGroup(4,
                        EPropGroup.of(4, some,
                                EProp.of(4, "p1", of(eq, "abc")),
                                EProp.of(5, "p2", of(eq, 1))),
                        EPropGroup.of(7, some,
                                EProp.of(7, "p3", of(gt, 6)),
                                EProp.of(8, "p4", of(inSet, Arrays.asList("s", "t"))))))
                .build();

        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void test_2_nested_some_all_quants_with_2_eProps() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, some))
                .in(quant1(3, all)
                                .next(eProp(4, "p1", of(eq, "abc")))
                                .next(eProp(5, "p2", of(eq, 1))),
                        quant1(6, all)
                                .next(eProp(7, "p3", of(gt, 6)))
                                .next(eProp(8, "p4", of(inSet, Arrays.asList("s", "t")))))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1")).next(quant1(2, some))
                .in(ePropGroup(4, some,
                        EPropGroup.of(4, all,
                                EProp.of(4, "p1", of(eq, "abc")),
                                EProp.of(5, "p2", of(eq, 1))),
                        EPropGroup.of(7, all,
                                EProp.of(7, "p3", of(gt, 6)),
                                EProp.of(8, "p4", of(inSet, Arrays.asList("s", "t"))))))
                .build();

        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void test_2_nested_all_some_quants_with_3_eProps() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(quant1(3, some)
                                .next(eProp(4, "p1", of(eq, "abc")))
                                .next(eProp(5, "p2", of(eq, 1)))
                                .next(eProp(6, "p3", of(gt, 6))),
                        quant1(7, some)
                                .next(eProp(8, "p4", of(inSet, Arrays.asList("s", "t"))))
                                .next(eProp(9, "p5", of(like, "*def*")))
                                .next(eProp(10, "p6", of(likeAny, Arrays.asList("s*", "*t")))))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(ePropGroup(4,
                        EPropGroup.of(4, some,
                                EProp.of(4, "p1", of(eq, "abc")),
                                EProp.of(5, "p2", of(eq, 1)),
                                EProp.of(6, "p3", of(gt, 6))),
                        EPropGroup.of(8, some,
                                EProp.of(8, "p4", of(inSet, Arrays.asList("s", "t"))),
                                EProp.of(9, "p5", of(like, "*def*")),
                                EProp.of(10, "p6", of(likeAny, Arrays.asList("s*", "*t"))))))
                .build();

        Assert.assertEquals(expectedQuery, query);
    }

    @Test
    public void test_2_nested_some_all_quants_with_3_eProps() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, some))
                .in(quant1(3, all)
                                .next(eProp(4, "p1", of(eq, "abc")))
                                .next(eProp(5, "p2", of(eq, 1)))
                                .next(eProp(6, "p3", of(gt, 6))),
                        quant1(7, all)
                                .next(eProp(8, "p4", of(inSet, Arrays.asList("s", "t"))))
                                .next(eProp(9, "p5", of(like, "*def*")))
                                .next(eProp(10, "p6", of(likeAny, Arrays.asList("s*", "*t")))))
                .build();

        new Quant1PropertiesGroupingAsgStrategy().apply(query, new AsgStrategyContext(null));

        AsgQuery expectedQuery = start("q1", "ont").next(typed(1, "1")).next(quant1(2, some))
                .in(ePropGroup(4, some,
                        EPropGroup.of(4, all,
                                EProp.of(4, "p1", of(eq, "abc")),
                                EProp.of(5, "p2", of(eq, 1)),
                                EProp.of(6, "p3", of(gt, 6))),
                        EPropGroup.of(8, all,
                                EProp.of(8, "p4", of(inSet, Arrays.asList("s", "t"))),
                                EProp.of(9, "p5", of(like, "*def*")),
                                EProp.of(10, "p6", of(likeAny, Arrays.asList("s*", "*t"))))))
                .build();

        Assert.assertEquals(expectedQuery, query);
    }
}
