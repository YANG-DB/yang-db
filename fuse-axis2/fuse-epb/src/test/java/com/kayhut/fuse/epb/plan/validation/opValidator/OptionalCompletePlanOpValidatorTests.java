package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.entity.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.composite.OptionalOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import org.junit.Assert;
import org.junit.Test;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.rel;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.typed;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * Created by roman.margolis on 26/11/2017.
 */
public class OptionalCompletePlanOpValidatorTests {
    //region Valid Tests
    @Test
    public void testValidPlan_entity3_optional8Xrel9_entity10X_goto3_rel2_entity1() {
        AsgQuery asgQuery = query1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new OptionalOp(AsgQueryUtil.element$(asgQuery, 8),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 9)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 10))),
                new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity3_optional16Xrel17_entity18_optional19Xrel20_entity21XX_goto3_rel2_entity1() {
        AsgQuery asgQuery = query1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new OptionalOp(AsgQueryUtil.element$(asgQuery, 16),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 17)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 18)),
                        new OptionalOp(AsgQueryUtil.element$(asgQuery, 19),
                                new RelationOp(AsgQueryUtil.element$(asgQuery, 20)),
                                new EntityOp(AsgQueryUtil.element$(asgQuery, 21)))),
                new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }
    //endregion

    //region Invalid Tests
    @Test
    public void testInvalidPlan_entity3_optional8Xentity10X_goto3_rel2_entity1() {
        AsgQuery asgQuery = query1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new OptionalOp(AsgQueryUtil.element$(asgQuery, 8),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 10))),
                new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity3_optional16Xrel17_entity18X_goto3_rel2_entity1() {
        AsgQuery asgQuery = query1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new OptionalOp(AsgQueryUtil.element$(asgQuery, 16),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 17)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 18))),
                new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
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
    //endregion

    //region Fields
    private PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
            new CompositePlanOpValidator(
                    CompositePlanOpValidator.Mode.all,
                    new OptionalCompletePlanOpValidator()));

    //endregion
}
