package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.*;
import com.kayhut.fuse.epb.plan.extenders.AllDirectionsPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.CompositePlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import com.kayhut.fuse.epb.plan.validation.DummyValidator;
import com.kayhut.fuse.epb.plan.wrappers.SimpleWrapperFactory;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;
import com.kayhut.fuse.model.query.EConcrete;
import com.kayhut.fuse.model.query.EUntyped;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.queryAsg.AsgQuery;
import com.kayhut.fuse.model.queryAsg.EBaseAsg;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
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
        when(extensionStrategy.extendPlan(any(), any())).thenReturn(new LinkedList<>());
        when(extensionStrategy.extendPlan(isNull(DummyPlan.class),  any())).thenReturn(initialPlans);
        List<DummyPlan> extendedPlans = new LinkedList<>();
        extendedPlans.add(new DummyPlan(completePlanName));
        when(extensionStrategy.extendPlan(eq(initialPlans.get(0)), any())).thenReturn(extendedPlans);

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
        Pair<AsgQuery, EBaseAsg> query = BuilderTestUtil.createTwoEntitiesPathQuery();
        List<PlanExtensionStrategy<Plan, AsgQuery>> extenders = new LinkedList<>();
        extenders.add(new InitialPlanGeneratorExtensionStrategy());
        extenders.add(new AllDirectionsPlanExtensionStrategy());
        CompositePlanExtensionStrategy<Plan, AsgQuery> compositePlanExtensionStrategy = new CompositePlanExtensionStrategy<>(extenders);

        PlanPruneStrategy<Plan, SingleCost> pruneStrategy = new NoPruningPruneStrategy<>();
        PlanValidator<Plan, AsgQuery> validator = new DummyValidator<>();

        PlanWrapperFactory<Plan, AsgQuery, SingleCost> planWrapperFactory = new SimpleWrapperFactory();

        BottomUpPlanBuilderImpl<Plan, AsgQuery, SingleCost> bottomUpPlanBuilder = new BottomUpPlanBuilderImpl<>(compositePlanExtensionStrategy,
                                                                                                                pruneStrategy,
                                                                                                                pruneStrategy,
                                                                                                                validator,
                                                                                                                planWrapperFactory);


        Iterable<PlanWrapper<Plan, SingleCost>> planWrappers = bottomUpPlanBuilder.build(query.getLeft(), new DefaultChoiceCriteria<>());

        List<PlanWrapper<Plan, SingleCost>> planList = new LinkedList<>();
        planWrappers.forEach(planList::add);

        Assert.assertEquals(1, planList.size());
        Assert.assertEquals(3, planList.get(0).getPlan().getOps().size());
        Assert.assertTrue(planList.get(0).isPlanComplete());
    }


}
