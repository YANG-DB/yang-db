package com.yangdb.fuse.epb.plan;

import com.yangdb.fuse.dispatcher.epb.PlanPruneStrategy;
import com.yangdb.fuse.dispatcher.epb.PlanSelector;
import com.yangdb.fuse.dispatcher.epb.PlanValidator;
import com.yangdb.fuse.dispatcher.epb.CostEstimator;
import com.yangdb.fuse.epb.plan.estimation.dummy.DummyCostEstimator;
import com.yangdb.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.yangdb.fuse.epb.plan.extenders.CompositePlanExtensionStrategy;
import com.yangdb.fuse.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import com.yangdb.fuse.epb.plan.extenders.StepAdjacentDfsStrategy;
import com.yangdb.fuse.epb.plan.pruners.NoPruningPruneStrategy;
import com.yangdb.fuse.epb.plan.selectors.AllCompletePlanSelector;
import com.yangdb.fuse.epb.plan.validation.M1PlanValidator;
import com.yangdb.fuse.epb.utils.BuilderTestUtil;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.AsgEBaseContainer;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.PlanOp;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.query.EBase;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by moti on 2/23/2017.
 */
public class DfsBottomUpPlanSearcherTests {
    @Test
    public void TestBuilderSimplePath(){
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query.getLeft());

        Assert.assertEquals(3, plan.getPlan().getOps().size());
    }

    @Test
    public void TestBuilderSingleEntity(){

        Pair<AsgQuery, AsgEBase> query = BuilderTestUtil.createSingleEntityQuery();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query.getLeft());

        Assert.assertEquals(1, plan.getPlan().getOps().size());
    }

    @Test
    public void TestBuilderAllPaths() {
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        PlanWithCost<Plan, PlanDetailedCost> plan = planSearcher.search(query.getLeft());

        Assert.assertEquals(3, plan.getPlan().getOps().size());

        AsgEBase firstElement = query.getLeft().getStart().getNext().get(0);
        AsgEBase secondElement = (AsgEBase) firstElement.getNext().get(0);
        AsgEBase thirdElement = (AsgEBase) secondElement.getNext().get(0);
        boolean foundFirstPlan = false;

        List<PlanOp> ops = plan.getPlan().getOps();

        if (firstElement.geteNum() == ((AsgEBaseContainer) ops.get(0)).getAsgEbase().geteNum() &&
                secondElement.geteNum() == ((AsgEBaseContainer) ops.get(1)).getAsgEbase().geteNum() &&
                thirdElement.geteNum() == ((AsgEBaseContainer) ops.get(2)).getAsgEbase().geteNum()) {
            foundFirstPlan = true;
        }

        Assert.assertTrue(foundFirstPlan);
    }

    private BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> createBottomUpPlanSearcher() {
        CompositePlanExtensionStrategy<Plan, AsgQuery> compositePlanExtensionStrategy = new CompositePlanExtensionStrategy<>(
                new InitialPlanGeneratorExtensionStrategy(),
                new StepAdjacentDfsStrategy());

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new M1PlanValidator();

        CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> costEstimator =
                new DummyCostEstimator<>(new PlanDetailedCost());

        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> planSelector = new AllCompletePlanSelector<>();

        return new BottomUpPlanSearcher<>(
                compositePlanExtensionStrategy,
                pruneStrategy,
                pruneStrategy,
                planSelector,
                planSelector,
                validator,
                costEstimator);
    }


}
