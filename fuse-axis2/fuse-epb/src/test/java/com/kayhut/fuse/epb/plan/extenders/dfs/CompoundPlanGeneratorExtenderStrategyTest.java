package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.epb.plan.extenders.CompoundStepExtenderStrategy;
import com.kayhut.fuse.epb.tests.PlanMockUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import javaslang.collection.Stream;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.epb.tests.PlanMockUtils.PlanMockBuilder.mock;
import static org.junit.Assert.assertEquals;

public class CompoundPlanGeneratorExtenderStrategyTest {

    @Test
    public void test_simpleQuery0seedPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");

        CompoundStepExtenderStrategy strategy = new CompoundStepExtenderStrategy();
        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.empty(), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 4);
        PlanAssert.assertEquals(mock(asgQuery).entity(1).plan(), extendedPlans.get(0));
        PlanAssert.assertEquals(mock(asgQuery).entity(3).entityFilter(9).plan(), extendedPlans.get(1));
        PlanAssert.assertEquals(mock(asgQuery).entity(6).plan(), extendedPlans.get(2));
        PlanAssert.assertEquals(mock(asgQuery).entity(8).plan(), extendedPlans.get(3));
    }

    @Test
    public void test_simpleQuery1seedPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");

        Plan startPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));

        CompoundStepExtenderStrategy strategy = new CompoundStepExtenderStrategy();
        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(startPlan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 1);
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).entityFilter(9).plan(), extendedPlans.get(0));
    }

    @Test
    public void test_simpleQuery2ElementsPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");

        CompoundStepExtenderStrategy strategy = new CompoundStepExtenderStrategy();
        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(
                Optional.of(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).plan()), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 4);

        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(2).relFilter(10).entity(1).plan(), extendedPlans.get(0));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).plan(), extendedPlans.get(1));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(7).relFilter(11).entity(8).plan(), extendedPlans.get(2));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).goTo(1).rel(2).relFilter(10).entity(3).entityFilter(9).plan(), extendedPlans.get(3));
    }


    /**
     *
     */
    @Test
    public void test_simpleQuery4ElementsPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");

        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 10)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)));

        CompoundStepExtenderStrategy strategy = new CompoundStepExtenderStrategy();
        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 5);
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).rel(5).entity(3).entityFilter(9).plan(), extendedPlans.get(0));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(1).rel(2).relFilter(10).entity(3).entityFilter(9).plan(), extendedPlans.get(1));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(3).rel(2).relFilter(10).entity(1).plan(), extendedPlans.get(2));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(3).rel(5).entity(6).plan(), extendedPlans.get(3));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(3).rel(7).relFilter(11).entity(8).plan(), extendedPlans.get(4));
    }

    @Test
    public void test_simpleQuery5ElementsPlan() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery3("name", "ont");

        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 10)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)));

        CompoundStepExtenderStrategy strategy = new CompoundStepExtenderStrategy();
        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 6);
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).rel(5).entity(3).entityFilter(9).plan(), extendedPlans.get(0));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).rel(12).entity(13).plan(), extendedPlans.get(1));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(1).rel(2).relFilter(10).entity(3).entityFilter(9).plan(), extendedPlans.get(2));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(3).rel(2).relFilter(10).entity(1).plan(), extendedPlans.get(3));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(3).rel(5).entity(6).plan(), extendedPlans.get(4));
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).rel(5).entity(6).goTo(3).rel(7).relFilter(11).entity(8).plan(), extendedPlans.get(5));
    }


    //region Private Methods
    private <T extends EBase> AsgEBase<T> getAsgEBaseByEnum(AsgQuery asgQuery, int eNum) {
        return AsgQueryUtils.<T>getElement(asgQuery, eNum).get();
    }
    //endregion
}
