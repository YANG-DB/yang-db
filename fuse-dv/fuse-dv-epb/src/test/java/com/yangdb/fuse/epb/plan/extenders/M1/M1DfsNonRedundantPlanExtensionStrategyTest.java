package com.yangdb.fuse.epb.plan.extenders.M1;

import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.yangdb.fuse.dispatcher.epb.PlanValidator;
import com.yangdb.fuse.epb.plan.validation.M1PlanValidator;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.PlanAssert;
import com.yangdb.fuse.model.execution.plan.composite.OptionalOp;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.entity.EntityNoOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.entity.GoToEntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.typed;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static com.yangdb.fuse.model.query.quant.QuantType.all;

/**
 * Created by roman.margolis on 27/11/2017.
 */
public class M1DfsNonRedundantPlanExtensionStrategyTest {
    //region Tests
    @Test
    public void test_empty() {
        boolean a = EntityOp.class.isAssignableFrom(new GoToEntityOp(null).getClass());

        AsgQuery query = query1("name", "ont");

        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1))
        );

        List<Plan> extendedPlans = Stream.ofAll(this.planExtensionStrategy.extendPlan(Optional.empty(), query))
                .filter(extendedPlan -> this.planValidator.isPlanValid(extendedPlan, query).valid())
                .toJavaList();

        Assert.assertEquals(1, extendedPlans.size());
        PlanAssert.assertEquals(expectedPlan, extendedPlans.get(0));
    }

    @Test
    public void test_entity1() {
        AsgQuery query = query1("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)));

        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 3),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 4)),
                        new EntityOp(AsgQueryUtil.element$(query, 5))));

        List<Plan> extendedPlans = Stream.ofAll(this.planExtensionStrategy.extendPlan(Optional.of(plan), query))
                .filter(extendedPlan -> this.planValidator.isPlanValid(extendedPlan, query).valid())
                .toJavaList();

        Assert.assertEquals(1, extendedPlans.size());
        PlanAssert.assertEquals(expectedPlan, extendedPlans.get(0));
    }

    @Test
    public void test_entity1_optional3Xrel4_entity5X() {
        AsgQuery query = query1("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 3),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 4)),
                        new EntityOp(AsgQueryUtil.element$(query, 5))));

        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 3),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 4)),
                        new EntityOp(AsgQueryUtil.element$(query, 5)),
                        new OptionalOp(AsgQueryUtil.element$(query, 6),
                                new EntityNoOp(AsgQueryUtil.element$(query, 5)),
                                new RelationOp(AsgQueryUtil.element$(query, 7)),
                                new EntityOp(AsgQueryUtil.element$(query, 8)))));

        List<Plan> extendedPlans = Stream.ofAll(this.planExtensionStrategy.extendPlan(Optional.of(plan), query))
                .filter(extendedPlan -> this.planValidator.isPlanValid(extendedPlan, query).valid())
                .toJavaList();

        Assert.assertEquals(1, extendedPlans.size());
        PlanAssert.assertEquals(expectedPlan, extendedPlans.get(0));
    }

    @Test
    public void test_entity1_optional3Xrel4_entity5_optional6Xrel7_entity8XX() {
        AsgQuery query = query1("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 3),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 4)),
                        new EntityOp(AsgQueryUtil.element$(query, 5)),
                        new OptionalOp(AsgQueryUtil.element$(query, 6),
                                new EntityNoOp(AsgQueryUtil.element$(query, 5)),
                                new RelationOp(AsgQueryUtil.element$(query, 7)),
                                new EntityOp(AsgQueryUtil.element$(query, 8)))));

        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 3),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 4)),
                        new EntityOp(AsgQueryUtil.element$(query, 5)),
                        new OptionalOp(AsgQueryUtil.element$(query, 6),
                                new EntityNoOp(AsgQueryUtil.element$(query, 5)),
                                new RelationOp(AsgQueryUtil.element$(query, 7)),
                                new EntityOp(AsgQueryUtil.element$(query, 8)))),
                new GoToEntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 9),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 10)),
                        new EntityOp(AsgQueryUtil.element$(query, 11))));

        List<Plan> extendedPlans = Stream.ofAll(this.planExtensionStrategy.extendPlan(Optional.of(plan), query))
                .filter(extendedPlan -> this.planValidator.isPlanValid(extendedPlan, query).valid())
                .toJavaList();

        Assert.assertEquals(1, extendedPlans.size());
        PlanAssert.assertEquals(expectedPlan, extendedPlans.get(0));
    }

    @Test
    public void test_entity1_optional3Xrel4_entity5_optional6Xrel7_entity8XX_goto1_optional9Xrel10_entity11X() {
        AsgQuery query = query1("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 3),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 4)),
                        new EntityOp(AsgQueryUtil.element$(query, 5)),
                        new OptionalOp(AsgQueryUtil.element$(query, 6),
                                new EntityNoOp(AsgQueryUtil.element$(query, 5)),
                                new RelationOp(AsgQueryUtil.element$(query, 7)),
                                new EntityOp(AsgQueryUtil.element$(query, 8)))),
                new GoToEntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 9),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 10)),
                        new EntityOp(AsgQueryUtil.element$(query, 11))));

        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 3),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 4)),
                        new EntityOp(AsgQueryUtil.element$(query, 5)),
                        new OptionalOp(AsgQueryUtil.element$(query, 6),
                                new EntityNoOp(AsgQueryUtil.element$(query, 5)),
                                new RelationOp(AsgQueryUtil.element$(query, 7)),
                                new EntityOp(AsgQueryUtil.element$(query, 8)))),
                new GoToEntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 9),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 10)),
                        new EntityOp(AsgQueryUtil.element$(query, 11))),
                new GoToEntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 12),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 13)),
                        new EntityOp(AsgQueryUtil.element$(query, 14))));

        List<Plan> extendedPlans = Stream.ofAll(this.planExtensionStrategy.extendPlan(Optional.of(plan), query))
                .filter(extendedPlan -> this.planValidator.isPlanValid(extendedPlan, query).valid())
                .toJavaList();

        Assert.assertEquals(1, extendedPlans.size());
        PlanAssert.assertEquals(expectedPlan, extendedPlans.get(0));
    }

    @Test
    public void test_entity1_optional3Xrel4_entity5_optional6Xrel7_entity8XX_goto1_optional9Xrel10_entity11X_goto1_optional12Xrel13_entity14X() {
        AsgQuery query = query1("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 3),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 4)),
                        new EntityOp(AsgQueryUtil.element$(query, 5)),
                        new OptionalOp(AsgQueryUtil.element$(query, 6),
                                new EntityNoOp(AsgQueryUtil.element$(query, 5)),
                                new RelationOp(AsgQueryUtil.element$(query, 7)),
                                new EntityOp(AsgQueryUtil.element$(query, 8)))),
                new GoToEntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 9),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 10)),
                        new EntityOp(AsgQueryUtil.element$(query, 11))),
                new GoToEntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 12),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 13)),
                        new EntityOp(AsgQueryUtil.element$(query, 14))));

        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 3),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 4)),
                        new EntityOp(AsgQueryUtil.element$(query, 5)),
                        new OptionalOp(AsgQueryUtil.element$(query, 6),
                                new EntityNoOp(AsgQueryUtil.element$(query, 5)),
                                new RelationOp(AsgQueryUtil.element$(query, 7)),
                                new EntityOp(AsgQueryUtil.element$(query, 8)))),
                new GoToEntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 9),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 10)),
                        new EntityOp(AsgQueryUtil.element$(query, 11))),
                new GoToEntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 12),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 13)),
                        new EntityOp(AsgQueryUtil.element$(query, 14)),
                        new OptionalOp(AsgQueryUtil.element$(query, 15),
                                new EntityNoOp(AsgQueryUtil.element$(query, 14)),
                                new RelationOp(AsgQueryUtil.element$(query, 16)),
                                new EntityOp(AsgQueryUtil.element$(query, 17)))));

        List<Plan> extendedPlans = Stream.ofAll(this.planExtensionStrategy.extendPlan(Optional.of(plan), query))
                .filter(extendedPlan -> this.planValidator.isPlanValid(extendedPlan, query).valid())
                .toJavaList();

        Assert.assertEquals(1, extendedPlans.size());
        PlanAssert.assertEquals(expectedPlan, extendedPlans.get(0));
    }

    @Test
    public void test_entity1_optional3Xrel4_entity5_optional6Xrel7_entity8XX_goto1_optional9Xrel10_entity11X_goto1_optional12Xrel13_entity14_optional15Xrel16_entity17XX() {
        AsgQuery query = query1("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 3),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 4)),
                        new EntityOp(AsgQueryUtil.element$(query, 5)),
                        new OptionalOp(AsgQueryUtil.element$(query, 6),
                                new EntityNoOp(AsgQueryUtil.element$(query, 5)),
                                new RelationOp(AsgQueryUtil.element$(query, 7)),
                                new EntityOp(AsgQueryUtil.element$(query, 8)))),
                new GoToEntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 9),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 10)),
                        new EntityOp(AsgQueryUtil.element$(query, 11))),
                new GoToEntityOp(AsgQueryUtil.element$(query, 1)),
                new OptionalOp(AsgQueryUtil.element$(query, 12),
                        new EntityNoOp(AsgQueryUtil.element$(query, 1)),
                        new RelationOp(AsgQueryUtil.element$(query, 13)),
                        new EntityOp(AsgQueryUtil.element$(query, 14)),
                        new OptionalOp(AsgQueryUtil.element$(query, 15),
                                new EntityNoOp(AsgQueryUtil.element$(query, 14)),
                                new RelationOp(AsgQueryUtil.element$(query, 16)),
                                new EntityOp(AsgQueryUtil.element$(query, 17)))));

        List<Plan> extendedPlans = Stream.ofAll(this.planExtensionStrategy.extendPlan(Optional.of(plan), query))
                .filter(extendedPlan -> this.planValidator.isPlanValid(extendedPlan, query).valid())
                .toJavaList();

        Assert.assertEquals(0, extendedPlans.size());
    }
    //endregion

    //region Queries
    private static AsgQuery query1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "entity1", "A"))
                .next(quant1(2, all))
                .in(
                        optional(3).next(rel(4, "rel1", R).next(typed(5, "entity2", "B")
                                .next(optional(6).next(rel(7, "rel2", R).next(typed(8, "entity3", "C")))))),
                        optional(9).next(rel(10, "rel3", R).next(typed(11, "entity4", "D"))),
                        optional(12).next(rel(13, "rel4", R).next(typed(14, "entity5", "E")
                                .next(optional(15).next(rel(16, "rel5", R).next(typed(17, "entity6", "F")))))))
                .build();
    }
    //endregion

    //region Fields
    private PlanExtensionStrategy<Plan, AsgQuery> planExtensionStrategy = new M1DfsNonRedundantPlanExtensionStrategy();
    private PlanValidator<Plan, AsgQuery> planValidator = new M1PlanValidator();
    //endregion
}
