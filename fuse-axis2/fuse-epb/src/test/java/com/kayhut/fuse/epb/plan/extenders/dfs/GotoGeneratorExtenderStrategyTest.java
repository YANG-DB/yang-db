package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.extenders.GotoExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class GotoGeneratorExtenderStrategyTest {
    @Test
    public void test_simpleQuery2_secondPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)));

        List<Plan> extendedPlans = Stream.ofAll(new GotoExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();
        Assert.assertTrue(extendedPlans.size() == 1);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(0).getOps().size()-1) instanceof GoToEntityOp);
    }

    @Test
    public void test_complexQuery2_secondPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)),
                new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 7)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 11)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 8)));

        List<Plan> extendedPlans = Stream.ofAll(new GotoExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();
        Assert.assertTrue(extendedPlans.size() == 3);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(0).getOps().size()-1) instanceof GoToEntityOp);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(1).getOps().size()-1) instanceof GoToEntityOp);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(2).getOps().size()-1) instanceof GoToEntityOp);
    }

    @Test
    public void test_simpleQuery2_thirdPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");


        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)));

        List<Plan> extendedPlans = Stream.ofAll(new GotoExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();
        Assert.assertTrue(extendedPlans.size() == 2);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(0).getOps().size()-1) instanceof GoToEntityOp);
        Assert.assertEquals(extendedPlans.get(0).getOps().size(),8);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(1).getOps().size()-1) instanceof GoToEntityOp);
        Assert.assertEquals(extendedPlans.get(1).getOps().size(),8);
    }

    @Test
    public void test_simpleQuery3_thirdPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery3("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 12)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 13)));

        List<Plan> extendedPlans = Stream.ofAll(new GotoExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();
        Assert.assertTrue(extendedPlans.size() == 3);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(0).getOps().size()-1) instanceof GoToEntityOp);
        Assert.assertEquals(extendedPlans.get(0).getOps().size(),10);
        Assert.assertTrue(extendedPlans.get(1).getOps().get(extendedPlans.get(1).getOps().size()-1) instanceof GoToEntityOp);
        Assert.assertEquals(extendedPlans.get(1).getOps().size(),10);
        Assert.assertTrue(extendedPlans.get(2).getOps().get(extendedPlans.get(2).getOps().size()-1) instanceof GoToEntityOp);
        Assert.assertEquals(extendedPlans.get(2).getOps().size(),10);
    }
}
