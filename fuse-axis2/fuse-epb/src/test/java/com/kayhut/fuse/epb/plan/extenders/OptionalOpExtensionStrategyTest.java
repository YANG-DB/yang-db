package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanAssert;
import com.kayhut.fuse.model.execution.plan.composite.OptionalOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * Created by roman.margolis on 26/11/2017.
 */
public class OptionalOpExtensionStrategyTest {
    //region Tests
    @Test
    public void test_query1_OptionalOpComplete() {
        AsgQuery asgQuery = query1("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new OptionalOp(AsgQueryUtil.element$(asgQuery, 8),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 9)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 10))));

        List<Plan> extendedPlans = Stream.ofAll(getPlanExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertEquals(0, extendedPlans.size());
    }

    @Test
    public void test_query1_OptionalOp_DownstreamStepExtension() {
        AsgQuery asgQuery = query1("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new OptionalOp(AsgQueryUtil.element$(asgQuery, 11),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 12)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 13))));

        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new OptionalOp(AsgQueryUtil.element$(asgQuery, 11),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 12)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 13)),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 14)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 15))));

        List<Plan> extendedPlans = Stream.ofAll(getPlanExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertEquals(1, extendedPlans.size());
        PlanAssert.assertEquals(expectedPlan, extendedPlans.get(0));
    }
    //endregion

    @Test
    public void test_query1_OptionalOp_DownstreamStepExtension_WithInnerOptional() {
        AsgQuery asgQuery = query1("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new OptionalOp(AsgQueryUtil.element$(asgQuery, 16),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 17)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 18))));

        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new OptionalOp(AsgQueryUtil.element$(asgQuery, 16),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 17)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 18)),
                        new OptionalOp(AsgQueryUtil.element$(asgQuery, 19),
                            new RelationOp(AsgQueryUtil.element$(asgQuery, 20)),
                            new EntityOp(AsgQueryUtil.element$(asgQuery, 21)))));

        List<Plan> extendedPlans = Stream.ofAll(getPlanExtensionStrategy().extendPlan(Optional.of(plan), asgQuery))
                .toJavaList();

        Assert.assertEquals(1, extendedPlans.size());
        PlanAssert.assertEquals(expectedPlan, extendedPlans.get(0));
    }

    @Test
    public void test_query1_OptionalOp_DownstreamStepExtension_Complete() {
        AsgQuery asgQuery = query1("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new OptionalOp(AsgQueryUtil.element$(asgQuery, 16),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 17)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 18)),
                        new OptionalOp(AsgQueryUtil.element$(asgQuery, 19),
                                new RelationOp(AsgQueryUtil.element$(asgQuery, 20)),
                                new EntityOp(AsgQueryUtil.element$(asgQuery, 21)))));

        List<Plan> extendedPlans = Stream.ofAll(getPlanExtensionStrategy().extendPlan(Optional.of(plan), asgQuery))
                .toJavaList();

        Assert.assertEquals(0, extendedPlans.size());
    }
    //endregion

    //region Queries
    private static AsgQuery query1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "entity1", "A"))
                .next(rel(2, "rel1", R).below(relProp(2, RelProp.of("2", 2, of(eq, "value2")))))
                .next(typed(3, "entity2", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(5, EProp.of("prop1", 5, of(eq, "value1")), EProp.of("prop2", 5, of(gt, "value3"))),
                        rel(6, "rel2", R).next(typed(7, "entity3", "C")),
                        optional(8).next(rel(9, "rel3", R).next(typed(10, "entity4", "D"))),
                        optional(11).next(rel(12, "rel4", R).next(typed(13, "entity4", "E")
                                .next(rel(14, "rel4", R).next(typed(15, "entity4", "F"))))),
                        optional(16).next(rel(17, "rel4", R).next(typed(18, "entity4", "G")
                                .next(optional(19).next(rel(20, "rel4", R).next(typed(21, "entity4", "H")))))))
                .build();
    }

    private static PlanExtensionStrategy<Plan, AsgQuery> getPlanExtensionStrategy() {
        return new OptionalOpExtensionStrategy(
                new ChainPlanExtensionStrategy<>(
                        new CompositePlanExtensionStrategy<>(
                                new StepAdjacentDfsStrategy(),
                                new OptionalOpExtensionStrategy(
                                        new ChainPlanExtensionStrategy<>(
                                                new CompositePlanExtensionStrategy<>(
                                                        new StepAdjacentDfsStrategy(),
                                                        new OptionalOpExtensionStrategy()),
                                                new OptionalInitialExtensionStrategy()))),
                        new OptionalInitialExtensionStrategy()));
    }
    //endregion
}
