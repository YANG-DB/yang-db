package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.cost.DummyCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.AllDirectionsPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.CompositePlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.query.EBase;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 2/28/2017.
 */
public class SimpleBuilderTests {

    @Test
    public void TestInitialCreationSingleEntity(){
        Pair<AsgQuery, AsgEBase> query = BuilderTestUtil.createSingleEntityQuery();

        InitialPlanGeneratorExtensionStrategy strategy = new InitialPlanGeneratorExtensionStrategy();
        Iterable<Plan> plans = strategy.extendPlan(Optional.empty(), query.getLeft());
        List<Plan> plansList = new LinkedList<>();
        plans.forEach(plansList::add);

        Assert.assertEquals(1,plansList.size());
        Plan plan = plansList.get(0);
        Assert.assertEquals(1,plan.getOps().size());
        PlanOpBase op = plan.getOps().get(0);
        Assert.assertTrue(op instanceof EntityOp);
        Assert.assertEquals(op.geteNum(), query.getRight().geteNum());
        Assert.assertEquals(query.getLeft().getStart().getNext().get(0), ((EntityOp)op).getAsgEBase());
    }

    @Test
    public void TestInitialCreationMultipleEntities(){
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        InitialPlanGeneratorExtensionStrategy strategy = new InitialPlanGeneratorExtensionStrategy();
        Iterable<Plan> plans = strategy.extendPlan(Optional.empty(), query.getLeft());
        List<Plan> plansList = new LinkedList<>();
        plans.forEach(plansList::add);

        Assert.assertEquals(2,plansList.size());

        AsgEBase<? extends EBase> untypedBaseAsg = query.getRight().getNext().get(0).getNext().get(0);

        boolean foundFirst = false, foundSecond = false;
        for(Plan plan : plans){
            Assert.assertEquals(1,plan.getOps().size());
            PlanOpBase op = plan.getOps().get(0);
            Assert.assertTrue(op instanceof EntityOp);
            if(((EntityOp)op).getAsgEBase().geteBase() == query.getRight().geteBase()){
                foundFirst = true;
                Assert.assertEquals(query.getRight().geteBase().geteNum(), op.geteNum());
            }

            if(((EntityOp)op).getAsgEBase().geteBase() == query.getRight().getNext().get(0).getNext().get(0).geteBase()) {
                foundSecond = true;
                Assert.assertEquals(untypedBaseAsg.geteNum(), op.geteNum());
            }
        }
        Assert.assertTrue(foundFirst && foundSecond);
    }

    @Test
    public void TestCompositePlanStrategyInit(){
        CompositePlanExtensionStrategy<Plan, AsgQuery> compositePlanExtensionStrategy =
                new CompositePlanExtensionStrategy<>(
                        new InitialPlanGeneratorExtensionStrategy(),
                        new AllDirectionsPlanExtensionStrategy());

        Pair<AsgQuery, AsgEBase> query = BuilderTestUtil.createSingleEntityQuery();

        Iterable<Plan> plans = compositePlanExtensionStrategy.extendPlan(Optional.empty(), query.getLeft());
        List<Plan> planList = new LinkedList<>();
        plans.forEach(p -> planList.add(p));

        Assert.assertEquals(1, planList.size());
        Plan plan = planList.get(0);
        Assert.assertEquals(1,plan.getOps().size());
        PlanOpBase op = plan.getOps().get(0);
        Assert.assertTrue(op instanceof EntityOp);
        Assert.assertEquals(op.geteNum(), query.getRight().geteNum());
        Assert.assertEquals(query.getLeft().getStart().getNext().get(0), ((EntityOp)op).getAsgEBase());
    }


}
