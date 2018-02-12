package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.validation.ValidationResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.END_DATE;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.ConstraintOp.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.RelProp.of;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

public class M2PlanValidatorTests extends M1PlanValidatorTests{

    public M2PlanValidatorTests() {
        this.validator = new M2PlanValidator();
    }

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

    @Test
    public void testJoinInValidLeftPlanSimpleQueryPartialPlan(){
        AsgQuery query = simpleQuery1("q", "ont");
        Plan plan = new Plan(
                new EntityJoinOp(new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)))
                        , new Plan(new EntityOp(AsgQueryUtil.element$(query,3)))));
        ValidationResult planValid = this.validator.isPlanValid(plan, query);
        Assert.assertFalse(planValid.valid());

    }

    @Test
    public void testJoinInValidPlanSimpleQuery(){
        AsgQuery query = simpleQuery1("q", "ont");
        Plan plan = new Plan(
                new EntityJoinOp(new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)))
                        , new Plan(new EntityOp(AsgQueryUtil.element$(query,3)),
                        new RelationOp(reverseRelation(AsgQueryUtil.element$(query,2))),
                        new EntityOp(AsgQueryUtil.element$(query,1)))));
        ValidationResult planValid = this.validator.isPlanValid(plan, query);
        Assert.assertFalse(planValid.valid());

    }
    @Test
    public void testJoinInValidPlanSimpleQueryPartialPlan(){
        AsgQuery query = simpleQuery1("q", "ont");
        Plan plan = new Plan(
                new EntityJoinOp(new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)))
                        , new Plan(new EntityOp(AsgQueryUtil.element$(query,1)),
                        new RelationOp(AsgQueryUtil.element$(query,2)),
                        new EntityOp(AsgQueryUtil.element$(query,3)))));
        ValidationResult planValid = this.validator.isPlanValid(plan, query);
        Assert.assertFalse(planValid.valid());

    }

    @Test
    public void testJoinHierarchy(){
        AsgQuery query = simpleQuery2("q", "ont");
        Plan left1 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),
                new RelationOp(AsgQueryUtil.element$(query, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 10)),
                new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));
        Plan right1 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 6)),
                new RelationOp(reverseRelation(AsgQueryUtil.element$(query, 5))),
                new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));

        EntityJoinOp joinOp1 = new EntityJoinOp(left1, right1, true);
        Plan right2 = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 8)),
                new RelationOp(reverseRelation(AsgQueryUtil.element$(query, 7))),
                new RelationFilterOp(AsgQueryUtil.element$(query, 11)),
                new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));
        EntityJoinOp joinOp2 = new EntityJoinOp(new Plan(joinOp1), right2,true);


        ValidationResult validationContext = this.validator.isPlanValid(new Plan(joinOp2), query);
        Assert.assertTrue(validationContext.valid());

    }

    @Test
    public void testJoinHierarchyInvalid(){
        AsgQuery query = simpleQuery2("q", "ont");
        Plan left1 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)));
        Plan right1 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 6)),
                new RelationOp(reverseRelation(AsgQueryUtil.element$(query, 5))),
                new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));

        EntityJoinOp joinOp1 = new EntityJoinOp(left1, right1);
        Plan right2 = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 8)),
                new RelationOp(reverseRelation(AsgQueryUtil.element$(query, 7))),
                new RelationFilterOp(AsgQueryUtil.element$(query, 11)),
                new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));
        EntityJoinOp joinOp2 = new EntityJoinOp(new Plan(joinOp1), right2);


        ValidationResult validationContext = this.validator.isPlanValid(new Plan(joinOp2), query);
        Assert.assertFalse(validationContext.valid());

    }

    @Test
    public void testJoinHierarchyInvalidRightBranch(){
        AsgQuery query = simpleQuery2("q", "ont");
        Plan left1 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),
                new RelationOp(AsgQueryUtil.element$(query, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 10)),
                new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));
        Plan right1 = new Plan(new EntityOp(AsgQueryUtil.element$(query, 6)));

        EntityJoinOp joinOp1 = new EntityJoinOp(left1, right1);
        Plan right2 = new Plan(
                new EntityOp(AsgQueryUtil.element$(query, 8)),
                new RelationOp(reverseRelation(AsgQueryUtil.element$(query, 7))),
                new RelationFilterOp(AsgQueryUtil.element$(query, 11)),
                new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));
        EntityJoinOp joinOp2 = new EntityJoinOp(new Plan(joinOp1), right2);


        ValidationResult validationContext = this.validator.isPlanValid(new Plan(joinOp2), query);
        Assert.assertFalse(validationContext.valid());

    }
}
