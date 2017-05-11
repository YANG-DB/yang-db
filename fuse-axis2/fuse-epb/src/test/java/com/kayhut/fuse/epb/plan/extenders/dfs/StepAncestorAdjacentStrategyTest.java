package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.epb.plan.extenders.StepAncestorAdjacentStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.model.execution.plan.Direction.reverse;

/**
 * Created by Roman on 23/04/2017.
 */
public class StepAncestorAdjacentStrategyTest {
    @Test
    public void test_simpleQuery1_seedPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 0);
    }

    @Test
    public void test_simpleQuery1_fullPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        AsgEBase<Rel> relation = getAsgEBaseByEnum(asgQuery, 2);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));
        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));

        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 0);
    }

    @Test
    public void test_simpleQuery2_fullPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        AsgEBase<Rel> relation = getAsgEBaseByEnum(asgQuery, 2);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));
        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));

        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 3)))), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_simpleQuery2ReversefullPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 10)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)));


        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        AsgEBase<Rel> relation = getAsgEBaseByEnum(asgQuery, 2);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));

        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 10)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 10)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_simpleQuery2_seedPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        AsgEBase<Rel> relation = getAsgEBaseByEnum(asgQuery, 5);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));
        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new EntityFilterOp(getAsgEBaseByEnum(asgQuery, 9)));

        Plan plan = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 6)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_simpleQuery2GotoPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        AsgEBase<Rel> relation = getAsgEBaseByEnum(asgQuery, 5);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));
        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new EntityFilterOp(getAsgEBaseByEnum(asgQuery, 9)));

        Plan plan = new Plan(new GoToEntityOp(getAsgEBaseByEnum(asgQuery, 6)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_simpleQuery2_secondPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        AsgEBase<Rel> relation = getAsgEBaseByEnum(asgQuery, 7);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));

        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 8)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 7)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 11)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new EntityFilterOp(getAsgEBaseByEnum(asgQuery, 9)));

        Plan plan = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 8)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }


    //region Private Methods
    private <T extends EBase> AsgEBase<T> getAsgEBaseByEnum(AsgQuery asgQuery, int eNum) {
        return AsgQueryUtils.<T>getElement(asgQuery, eNum).get();
    }
    //endregion
}
