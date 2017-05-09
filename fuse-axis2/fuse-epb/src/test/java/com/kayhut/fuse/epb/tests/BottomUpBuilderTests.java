package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.*;
import com.kayhut.fuse.epb.plan.cost.CostEstimator;
import com.kayhut.fuse.epb.plan.cost.DummyCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.AllDirectionsPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.CompositePlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import com.kayhut.fuse.epb.plan.validation.M1PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.query.EBase;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by moti on 2/23/2017.
 */
public class BottomUpBuilderTests {
    @Test
    public void TestBasicBuilderBehavior() {
        /*
        class DummyPlan {
            String name;

            public DummyPlan(String name) {
                this.name = name;
            }
        }

        class DummyQuery {

        }

        class DummyCost{

        }

        String completePlanName = "P2";

        PlanExtensionStrategy<DummyPlan, DummyQuery> extensionStrategy = Mockito.mock(PlanExtensionStrategy.class);
        List<DummyPlan> initialPlans = new LinkedList<>();
        initialPlans.add(new DummyPlan("P1"));
        List<DummyPlan> extendedPlans = new LinkedList<>();
        extendedPlans.add(new DummyPlan(completePlanName));


        when(extensionStrategy.extendPlan(any(), any())).thenAnswer(invocationOnMock -> {
            Optional<DummyPlan> optional = (Optional<DummyPlan>)invocationOnMock.getArguments()[0];
            if(optional.isPresent()){
                if(optional.get() == initialPlans.get(0)){
                    return extendedPlans;
                }else{
                    return new LinkedList<>();
                }
            }else{
                return initialPlans;
            }
        });

        PlanPruneStrategy<DummyPlan> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<DummyPlan, DummyQuery> validator = new DummyValidator<>();

        BottomUpPlanBuilderImpl<DummyPlan, DummyQuery> planBuilder = new BottomUpPlanBuilderImpl<>(extensionStrategy,
                pruneStrategy,
                pruneStrategy,
                validator);
        Iterable<DummyPlan> plans = planBuilder.build(new DummyQuery(), new DefaultChoiceCriteria<DummyPlan>());

        Assert.assertNotNull(plans);
        Iterator<PlanWrapper<DummyPlan, DummyCost>> planWrapperIterator = plans.iterator();
        Assert.assertTrue(planWrapperIterator.hasNext());
        PlanWrapper<DummyPlan, DummyCost> planWrapper = planWrapperIterator.next();
        Assert.assertEquals(planWrapper.getPlan(), extendedPlans.get(0));
        Assert.assertFalse(planWrapperIterator.hasNext());
        */
    }

    @Test
    public void TestBuilderSimplePath(){
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        BottomUpPlanBuilderImpl<Plan, PlanDetailedCost, AsgQuery> bottomUpPlanBuilder = createBottomUpPlanBuilder();


        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = bottomUpPlanBuilder.build(query.getLeft(), new DefaultChoiceCriteria<>());

        List<PlanWithCost<Plan, PlanDetailedCost>> planList = new LinkedList<>();
        plans.forEach(planList::add);

        Assert.assertEquals(1, planList.size());
        Assert.assertEquals(3, planList.get(0).getPlan().getOps().size());
    }

    @Test
    public void TestBuilderSingleEntity(){

        Pair<AsgQuery, AsgEBase> query = BuilderTestUtil.createSingleEntityQuery();

        BottomUpPlanBuilderImpl<Plan, PlanDetailedCost, AsgQuery> bottomUpPlanBuilder = createBottomUpPlanBuilder();


        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = bottomUpPlanBuilder.build(query.getLeft(), new DefaultChoiceCriteria<>());

        List<PlanWithCost<Plan, PlanDetailedCost>> planList = new LinkedList<>();
        plans.forEach(planList::add);

        Assert.assertEquals(1, planList.size());
        Assert.assertEquals(1, planList.get(0).getPlan().getOps().size());
    }

    @Test
    public void TestBuilderAllPaths(){
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        BottomUpPlanBuilderImpl<Plan, PlanDetailedCost, AsgQuery> bottomUpPlanBuilder = createBottomUpPlanBuilder();


        Iterable<PlanWithCost<Plan, PlanDetailedCost>> planWrappers = bottomUpPlanBuilder.build(query.getLeft(), new DefaultAllCompletePlansChoiceCriteria<>());

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

            if(firstElement.geteNum() == ops.get(2).geteNum()
                    && secondElement.geteNum() == ops.get(1).geteNum()
                    && thirdElement.geteNum() == ops.get(0).geteNum()) {
            }
        }
        Assert.assertTrue(foundFirstPlan );
    }

    private BottomUpPlanBuilderImpl<Plan, PlanDetailedCost, AsgQuery> createBottomUpPlanBuilder() {
        CompositePlanExtensionStrategy<Plan, AsgQuery> compositePlanExtensionStrategy = new CompositePlanExtensionStrategy<>(
                new InitialPlanGeneratorExtensionStrategy(),
                new AllDirectionsPlanExtensionStrategy());

        PlanPruneStrategy<PlanWithCost<Plan, PlanDetailedCost>> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new M1PlanValidator();

        CostEstimator<Plan, PlanDetailedCost> costEstimator = new DummyCostEstimator<>(new PlanDetailedCost());

        return new BottomUpPlanBuilderImpl<>(compositePlanExtensionStrategy,
                pruneStrategy,
                pruneStrategy,
                validator,
                costEstimator);
    }


}
