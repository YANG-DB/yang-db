package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 23/04/2017.
 */
public class StepAdjacentStrategyTest {
    @Test
    public void test_startXeTypedXrelXeTypedXXX_seedPlan() {
        AsgQuery asgQuery = AsgQueryStore.startXeTypedXrelXeTypedXXX("name", "ont");
        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)));

        Plan plan = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_startXeTypedXrelXeTypedXXX_fullPlan() {
        AsgQuery asgQuery = AsgQueryStore.startXeTypedXrelXeTypedXXX("name", "ont");
        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)));

        List<Plan> extendedPlans = Stream.ofAll(new StepAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 0);
    }

    @Test
    public void test_startXeTypedXrelXeTypedXQuant1XrelXunTypedX_relXconcreteXXXXXX_seedPlan() {
        AsgQuery asgQuery = AsgQueryStore.startXeTypedXrelXeTypedXQuant1XrelXunTypedX_relXconcreteXXXXXX("name", "ont");
        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)));

        Plan plan = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test

    public void test_startXeTypedXrelXeTypedXQuant1XrelXunTypedX_relXconcreteXXXXXX_secondPlan() {
        AsgQuery asgQuery = AsgQueryStore.startXeTypedXrelXeTypedXQuant1XrelXunTypedX_relXconcreteXXXXXX("name", "ont");

        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)));

        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)));

        List<Plan> extendedPlans = Stream.ofAll(new StepAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_startXeTypedXrelXeTypedXQuant1XrelXunTypedX_relXconcreteXXXXXX_thirdPlan() {
        AsgQuery asgQuery = AsgQueryStore.startXeTypedXrelXeTypedXQuant1XrelXunTypedX_relXconcreteXXXXXX("name", "ont");

        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 7)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 8)));

        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)));

        List<Plan> extendedPlans = Stream.ofAll(new StepAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    //region Private Methods
    private <T extends EBase> AsgEBase<T> getAsgEBaseByEnum(AsgQuery asgQuery, int eNum) {
        return AsgQueryUtils.<Start, T>getNextDescendant(asgQuery.getStart(), eNum).get();
    }
    //endregion
}
