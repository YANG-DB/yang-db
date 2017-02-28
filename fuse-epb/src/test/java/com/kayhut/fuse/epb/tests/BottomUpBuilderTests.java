package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.*;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
}
