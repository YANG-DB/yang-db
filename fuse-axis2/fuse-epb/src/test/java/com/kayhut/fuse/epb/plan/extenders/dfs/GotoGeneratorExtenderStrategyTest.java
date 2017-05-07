package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.epb.plan.extenders.ChainPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.GotoExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.StepAncestorAdjacentStrategy;
import com.kayhut.fuse.epb.plan.extenders.StepDescendantsAdjacentStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.model.execution.plan.Direction.reverse;
import static org.junit.Assert.assertEquals;

public class GotoGeneratorExtenderStrategyTest {
    @Test
    public void test_simpleQuery2_secondPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");

        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 10)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new EntityFilterOp(getAsgEBaseByEnum(asgQuery, 9)));

        List<Plan> extendedPlans = Stream.ofAll(new GotoExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();
        Assert.assertTrue(extendedPlans.size() == 1);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(0).getOps().size()-1) instanceof GoToEntityOp);
    }

    @Test
    public void test_complexQuery2_secondPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");

        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 10)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new EntityFilterOp(getAsgEBaseByEnum(asgQuery, 9)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)),
                new GoToEntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 7)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 11)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 8)));

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
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 10)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new EntityFilterOp(getAsgEBaseByEnum(asgQuery, 9)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)));

        List<Plan> extendedPlans = Stream.ofAll(new GotoExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();
        Assert.assertTrue(extendedPlans.size() == 2);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(0).getOps().size()-1) instanceof GoToEntityOp);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(1).getOps().size()-1) instanceof GoToEntityOp);
    }

    //region Private Methods
    private <T extends EBase> AsgEBase<T> getAsgEBaseByEnum(AsgQuery asgQuery, int eNum) {
        return AsgQueryUtils.<T>getElement(asgQuery, eNum).get();
    }
    //endregion
}
