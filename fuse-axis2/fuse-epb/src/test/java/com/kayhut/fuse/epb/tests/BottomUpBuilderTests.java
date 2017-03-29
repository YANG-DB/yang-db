package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.*;
import com.kayhut.fuse.epb.plan.cost.DummyPlanOpCostEstimator;
import com.kayhut.fuse.epb.plan.cost.PlanOpCostEstimator;
import com.kayhut.fuse.epb.plan.extenders.AllDirectionsPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.CompositePlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import com.kayhut.fuse.epb.plan.validation.DummyValidator;
import com.kayhut.fuse.epb.plan.validation.SiblingOnlyPlanValidator;
import com.kayhut.fuse.epb.plan.wrappers.SimpleWrapperFactory;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanOpWithCost;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;
import com.kayhut.fuse.model.query.EBase;
import javaslang.Tuple2;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by moti on 2/23/2017.
 */
public class BottomUpBuilderTests {
    @Test
    public void TestBasicBuilderBehavior() {
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

        PlanPruneStrategy<DummyPlan, DummyCost> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<DummyPlan, DummyQuery> validator = new DummyValidator<>();

        PlanWrapperFactory<DummyPlan,DummyQuery, DummyCost> wrapperFactory = Mockito.mock(PlanWrapperFactory.class);
        when(wrapperFactory.wrapPlan(any(), any())).thenAnswer(invocationOnMock -> new PlanWrapper<DummyPlan, DummyCost>() {
            @Override
            public DummyPlan getPlan() {
                return (DummyPlan) invocationOnMock.getArguments()[0];
            }

            @Override
            public DummyCost getPlanCost() {
                return new DummyCost();
            }

            @Override
            public Tuple2<DummyPlan, DummyCost> asTuple2() {
                return new Tuple2<>(getPlan(),getPlanCost());
            }

            @Override
            public boolean isPlanComplete() {
                return ((DummyPlan) invocationOnMock.getArguments()[0]).name.equals(completePlanName);
            }
        });

        BottomUpPlanBuilderImpl<DummyPlan, DummyQuery, DummyCost> planBuilder = new BottomUpPlanBuilderImpl<>(extensionStrategy,
                pruneStrategy,
                pruneStrategy,
                validator,
                wrapperFactory);
        Iterable<PlanWrapper<DummyPlan, DummyCost>> plans = planBuilder.build(new DummyQuery(), new DefaultChoiceCriteria<>());

        Assert.assertNotNull(plans);
        Iterator<PlanWrapper<DummyPlan, DummyCost>> planWrapperIterator = plans.iterator();
        Assert.assertTrue(planWrapperIterator.hasNext());
        PlanWrapper<DummyPlan, DummyCost> planWrapper = planWrapperIterator.next();
        Assert.assertEquals(planWrapper.getPlan(), extendedPlans.get(0));
        Assert.assertFalse(planWrapperIterator.hasNext());
    }

    @Test
    public void TestBuilderSimplePath(){
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        BottomUpPlanBuilderImpl<Plan<SingleCost>, AsgQuery, SingleCost> bottomUpPlanBuilder = createBottomUpPlanBuilder();


        Iterable<PlanWrapper<Plan<SingleCost>, SingleCost>> planWrappers = bottomUpPlanBuilder.build(query.getLeft(), new DefaultChoiceCriteria<>());

        List<PlanWrapper<Plan<SingleCost>, SingleCost>> planList = new LinkedList<>();
        planWrappers.forEach(planList::add);

        Assert.assertEquals(1, planList.size());
        Assert.assertEquals(3, planList.get(0).getPlan().getOps().size());
        Assert.assertTrue(planList.get(0).isPlanComplete());
    }

    @Test
    public void TestBuilderSingleEntity(){

        Pair<AsgQuery, AsgEBase> query = BuilderTestUtil.createSingleEntityQuery();

        BottomUpPlanBuilderImpl<Plan<SingleCost>, AsgQuery, SingleCost> bottomUpPlanBuilder = createBottomUpPlanBuilder();


        Iterable<PlanWrapper<Plan<SingleCost>, SingleCost>> planWrappers = bottomUpPlanBuilder.build(query.getLeft(), new DefaultChoiceCriteria<>());

        List<PlanWrapper<Plan<SingleCost>, SingleCost>> planList = new LinkedList<>();
        planWrappers.forEach(planList::add);

        Assert.assertEquals(1, planList.size());
        Assert.assertEquals(1, planList.get(0).getPlan().getOps().size());
        Assert.assertTrue(planList.get(0).isPlanComplete());
    }

    @Test
    public void TestBuilderAllPaths(){
        Pair<AsgQuery, AsgEBase<? extends EBase>> query = BuilderTestUtil.createTwoEntitiesPathQuery();

        BottomUpPlanBuilderImpl<Plan<SingleCost>, AsgQuery, SingleCost> bottomUpPlanBuilder = createBottomUpPlanBuilder();


        Iterable<PlanWrapper<Plan<SingleCost>, SingleCost>> planWrappers = bottomUpPlanBuilder.build(query.getLeft(), new DefaultAllCompletePlansChoiceCriteria<>());

        List<PlanWrapper<Plan<SingleCost>, SingleCost>> planList = new LinkedList<>();
        planWrappers.forEach(planList::add);

        Assert.assertEquals(2, planList.size());
        Assert.assertEquals(3, planList.get(0).getPlan().getOps().size());
        Assert.assertEquals(3, planList.get(1).getPlan().getOps().size());
        Assert.assertTrue(planList.get(0).isPlanComplete());
        Assert.assertTrue(planList.get(1).isPlanComplete());

        AsgEBase firstElement = query.getLeft().getStart().getNext().get(0);
        AsgEBase secondElement = (AsgEBase) firstElement.getNext().get(0);
        AsgEBase thirdElement = (AsgEBase) secondElement.getNext().get(0);
        boolean foundFirstPlan = false;
        boolean foundSecondPlan = false;
        for(PlanWrapper<Plan<SingleCost>, SingleCost> plan : planList){
            List<PlanOpWithCost<SingleCost>> ops = plan.getPlan().getOps();
            if(firstElement.geteNum() == ops.get(0).getOpBase().geteNum() && secondElement.geteNum() == ops.get(1).getOpBase().geteNum() && thirdElement.geteNum() == ops.get(2).getOpBase().geteNum())
                foundFirstPlan = true;
            if(firstElement.geteNum() == ops.get(2).getOpBase().geteNum() && secondElement.geteNum() == ops.get(1).getOpBase().geteNum() && thirdElement.geteNum() == ops.get(0).getOpBase().geteNum())
                foundSecondPlan = true;
        }
        Assert.assertTrue(foundFirstPlan && foundSecondPlan);
    }

    private BottomUpPlanBuilderImpl<Plan<SingleCost>, AsgQuery, SingleCost> createBottomUpPlanBuilder() {
        CompositePlanExtensionStrategy<Plan<SingleCost>, AsgQuery> compositePlanExtensionStrategy = new CompositePlanExtensionStrategy<>(new InitialPlanGeneratorExtensionStrategy<SingleCost>(new DummyPlanOpCostEstimator()),
                new AllDirectionsPlanExtensionStrategy<SingleCost>(new DummyPlanOpCostEstimator()));

        PlanPruneStrategy<Plan<SingleCost>, SingleCost> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<Plan<SingleCost>, AsgQuery> validator = new SiblingOnlyPlanValidator();

        PlanWrapperFactory<Plan<SingleCost>, AsgQuery, SingleCost> planWrapperFactory = new SimpleWrapperFactory();

        return new BottomUpPlanBuilderImpl<>(compositePlanExtensionStrategy,
                pruneStrategy,
                pruneStrategy,
                validator,
                planWrapperFactory);
    }


}
