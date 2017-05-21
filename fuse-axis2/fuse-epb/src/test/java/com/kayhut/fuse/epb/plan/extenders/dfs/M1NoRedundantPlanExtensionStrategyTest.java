package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.M1NoRedundantPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.M1PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import javaslang.collection.Stream;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.epb.tests.PlanMockUtils.PlanMockBuilder.mock;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static org.junit.Assert.assertEquals;

public class M1NoRedundantPlanExtensionStrategyTest {

    public static AsgQuery simpleQuery3(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "A", 1))
                .next(rel(R, 2, 1).below(relProp(10, RelProp.of("2", 10, of(eq, "value2")))))
                .next(typed(2, "B", 3))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of("1", 9, of(eq, "value1")), EProp.of("3", 9, of(gt, "value3")))
                        , rel(R, 5, 4)
                                .next(unTyped("C", 6)
                                        .next(rel(R, 12, 4)
                                                .next(typed(4, "G", 13))
                                        )
                                )
                        , rel(R, 7, 5)
                                .below(relProp(11, RelProp.of("5", 11, of(eq, "value5")), RelProp.of("4", 11, of(eq, "value4"))))
                                .next(concrete("concrete1", 3, "Concrete1", "D", 8)
                                        .next(rel(R, 14, 1)
                                                .next(typed(1, "F", 15))
                                        )
                                )
                )
                .build();
    }

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "A", 1))
                .next(rel(R, 2, 1).below(relProp(10, RelProp.of("2", 10, of(eq, "value2")))))
                .next(typed(2, "B", 3))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of("1", 9, of(eq, "value1")), EProp.of("3", 9, of(gt, "value3")))
                        , rel(R, 5, 4)
                                .next(unTyped("C", 6))
                        , rel(R, 7, 5)
                                .below(relProp(11, RelProp.of("5", 11, of(eq, "value5")), RelProp.of("4", 11, of(eq, "value4"))))
                                .next(concrete("concrete1", 3, "Concrete1", "D", 8))
                )
                .build();
    }

    @Test
    public void test_simpleQuery0seedPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        PlanExtensionStrategy<Plan, AsgQuery> strategy = new M1NoRedundantPlanExtensionStrategy();
        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.empty(), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 4);
        PlanAssert.assertEquals(mock(asgQuery).entity(1).plan(), extendedPlans.get(0));
        PlanAssert.assertEquals(mock(asgQuery).entity(3).entityFilter(9).plan(), extendedPlans.get(1));
        PlanAssert.assertEquals(mock(asgQuery).entity(6).plan(), extendedPlans.get(2));
        PlanAssert.assertEquals(mock(asgQuery).entity(8).plan(), extendedPlans.get(3));
    }

    @Test
    public void test_simpleQuery1seedPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        Plan startPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));

        PlanExtensionStrategy<Plan, AsgQuery> strategy = new M1NoRedundantPlanExtensionStrategy();
        List<Plan> extendedPlans = Stream.ofAll(strategy.extendPlan(Optional.of(startPlan), asgQuery)).toJavaList();

        assertEquals(extendedPlans.size(), 1);
        PlanAssert.assertEquals(mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).entityFilter(9).plan(), extendedPlans.get(0));
    }

    @Test
    public void test_simpleQuery2ElementsPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        PlanExtensionStrategy<Plan, AsgQuery> strategy = new M1NoRedundantPlanExtensionStrategy();
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
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 10)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)));

        PlanExtensionStrategy<Plan, AsgQuery> strategy = new M1NoRedundantPlanExtensionStrategy();
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
        AsgQuery asgQuery = simpleQuery3("name", "ont");

        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new RelationFilterOp(getAsgEBaseByEnum(asgQuery, 10)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)));

        PlanExtensionStrategy<Plan, AsgQuery> strategy = new M1NoRedundantPlanExtensionStrategy();
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
        return AsgQueryUtil.<T>getElement(asgQuery, eNum).get();
    }
    //endregion
}