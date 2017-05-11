package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.epb.plan.*;
import com.kayhut.fuse.epb.plan.cost.CostEstimator;
import com.kayhut.fuse.epb.plan.cost.DummyCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.CompositePlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.CompoundStepExtenderStrategy;
import com.kayhut.fuse.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.dfs.StepAdjacentStrategy;
import com.kayhut.fuse.epb.plan.validation.M1PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanAssert;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.query.EBase;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static com.kayhut.fuse.epb.tests.PlanMockUtils.PlanMockBuilder.mock;

/**
 * Created by moti on 2/23/2017.
 */
public class CompoundBottomUpPlanSearcherTests {
    @Test
    public void TestBuilderSimpleQ2Path(){
        AsgQuery query = AsgQueryStore.simpleQuery2("name", "o1");

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);

        List<PlanWithCost<Plan, PlanDetailedCost>> planList = new LinkedList<>();
        plans.forEach(planList::add);

        Assert.assertEquals(2, planList.size());
        PlanAssert.assertEquals(mock(query).entity(1).rel(2).relFilter(10).entity(3).entityFilter(9).rel(5).entity(6).goTo(3).rel(7).relFilter(11).entity(8).plan(), planList.get(0).getPlan());
        PlanAssert.assertEquals(mock(query).entity(1).rel(2).relFilter(10).entity(3).entityFilter(9).rel(7).relFilter(11).entity(8).goTo(3).rel(5).entity(6).plan(), planList.get(1).getPlan());
    }

    @Test
    public void TestBuilderSimpleQ1Path(){
        AsgQuery query = AsgQueryStore.simpleQuery1("name", "o1");

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query);

        List<PlanWithCost<Plan, PlanDetailedCost>> planList = new LinkedList<>();
        plans.forEach(planList::add);

        Assert.assertEquals(1, planList.size());
        Assert.assertEquals(3, planList.get(0).getPlan().getOps().size());
    }

    @Test
    public void TestBuilderSimplePath(){
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query.getLeft());

        List<PlanWithCost<Plan, PlanDetailedCost>> planList = new LinkedList<>();
        plans.forEach(planList::add);

        Assert.assertEquals(1, planList.size());
        Assert.assertEquals(3, planList.get(0).getPlan().getOps().size());
    }

    @Test
    public void TestBuilderSingleEntity(){

        Pair<AsgQuery, AsgEBase> query = BuilderTestUtil.createSingleEntityQuery();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = planSearcher.search(query.getLeft());

        List<PlanWithCost<Plan, PlanDetailedCost>> planList = new LinkedList<>();
        plans.forEach(planList::add);

        Assert.assertEquals(1, planList.size());
        Assert.assertEquals(1, planList.get(0).getPlan().getOps().size());
    }

    @Test
    public void TestBuilderAllPaths(){
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher = createBottomUpPlanSearcher();


        Iterable<PlanWithCost<Plan, PlanDetailedCost>> planWrappers = planSearcher.search(query.getLeft());

        List<PlanWithCost<Plan, PlanDetailedCost>> planList = new LinkedList<>();
        planWrappers.forEach(planList::add);

        Assert.assertEquals(1, planList.size());
        Assert.assertEquals(3, planList.get(0).getPlan().getOps().size());

        AsgEBase firstElement = query.getLeft().getStart().getNext().get(0);
        AsgEBase secondElement = (AsgEBase) firstElement.getNext().get(0);
        AsgEBase thirdElement = (AsgEBase) secondElement.getNext().get(0);
        boolean foundFirstPlan = false;
        for(PlanWithCost<Plan, PlanDetailedCost> planWithCost : planList){
            List<PlanOpBase> ops = planWithCost.getPlan().getOps();

            if(firstElement.geteNum() == ops.get(0).geteNum() &&
                    secondElement.geteNum() == ops.get(1).geteNum() &&
                    thirdElement.geteNum() == ops.get(2).geteNum()) {
                foundFirstPlan = true;
            }
        }
        Assert.assertTrue(foundFirstPlan);
    }

    private BottomUpPlanSearcher<Plan, PlanDetailedCost, AsgQuery> createBottomUpPlanSearcher() {

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new M1PlanValidator();

        CostEstimator<Plan, PlanDetailedCost> costEstimator = new DummyCostEstimator<>(new PlanDetailedCost());

        PlanSelector<PlanWithCost<Plan, PlanDetailedCost>, AsgQuery> planSelector = new AllCompletePlanSelector<>();

        return new BottomUpPlanSearcher<>(
                new CompoundStepExtenderStrategy(),
                pruneStrategy,
                pruneStrategy,
                planSelector,
                planSelector,
                validator,
                costEstimator);
    }


}
