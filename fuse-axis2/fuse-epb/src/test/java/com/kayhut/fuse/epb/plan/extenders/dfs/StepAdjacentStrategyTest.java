package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Start;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 23/04/2017.
 */
public class StepAdjacentStrategyTest {
    @Test
    public void test_simpleQuery1_seedPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
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
    public void test_simpleQuery1_fullPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)));

        List<Plan> extendedPlans = Stream.ofAll(new StepAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 0);
    }

    @Test
    public void test_simpleQuery2_seedPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new EntityFilterOp(getAsgEBaseByEnum(asgQuery, 9)));

        Plan plan = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test

    public void test_simpleQuery2_secondPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");

        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new EntityFilterOp(getAsgEBaseByEnum(asgQuery, 9)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)));

        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new EntityFilterOp(getAsgEBaseByEnum(asgQuery, 9)));

        List<Plan> extendedPlans = Stream.ofAll(new StepAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_simpleQuery2_thirdPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");

        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new EntityFilterOp(getAsgEBaseByEnum(asgQuery, 9)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)),
                new GoToEntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 7)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 8)));

        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new EntityFilterOp(getAsgEBaseByEnum(asgQuery, 9)),
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
