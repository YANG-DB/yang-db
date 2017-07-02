package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EProp;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.END_DATE;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.concrete;
import static com.kayhut.fuse.model.query.ConstraintOp.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.RelProp.of;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * Created by Roman on 30/04/2017.
 */
public class NoRedundantRelationOpValidatorTests {

    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, OntologyTestUtils.DRAGON.type,"B")).build();
    }
    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        long time = System.currentTimeMillis();
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(rel(2, OWN.getrType(), R).below(relProp(10, of(START_DATE.type, 10, Constraint.of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.DRAGON.type))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of(NAME.type, 9, Constraint.of(eq, "smith")), EProp.of(GENDER.type, 9, Constraint.of(gt, MALE)))
                        , rel(5, FREEZE.getrType(), R)
                                .next(unTyped(6))
                        , rel(7, FIRE.getrType(), R)
                                .below(relProp(11, of(START_DATE.type, 11,
                                        Constraint.of(ge, new Date(time - 1000 * 60))),
                                        of(END_DATE.type, 11, Constraint.of(le, new Date(time + 1000 * 60)))))
                                .next(concrete(8, "smoge", DRAGON.type, "Display:smoge", "D"))
                )
                .build();
    }

    //region Valid Plan Tests
    @Test
    public void testValidPlan_entity1_rel2() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel5() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery).valid());
    }
    //endregion

    //region Invalid Plan Tests
    @Test
    public void testInvalidPlan_entity1_rel2_rel2() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity1_rel2_entity3_rel2() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity1_rel2_entity3_rel5_rel2() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity1_rel2_entity3_rel5_rel5() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity6_rel5_entity3_rel5() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }

    @Test
    public void testInvalidPlan_entity6_rel5_entity3_rel2_rel5() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 6).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 2).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(asgQuery, 5).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery).valid());
    }
    //endregion

    //region Fields
    private PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
            new CompositePlanOpValidator(
                    CompositePlanOpValidator.Mode.all,
                    new NoRedundantRelationOpValidator()));

    //endregion
}
