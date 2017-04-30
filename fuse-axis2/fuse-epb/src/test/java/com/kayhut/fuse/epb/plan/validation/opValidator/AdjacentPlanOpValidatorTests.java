package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Roman on 26/04/2017.
 */
public class AdjacentPlanOpValidatorTests {
    @Test
    public void testValidPlan_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 1).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 3).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 1).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 2).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 3).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 2).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 1).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 2).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 3).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_rel2_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 3).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 2).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 1).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel5() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 1).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 2).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 3).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 5).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel5_entity6() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 1).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 2).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 3).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 5).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 6).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity6_rel5_entity3_rel2_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 6).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 5).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 3).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 2).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 1).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel7() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 1).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 2).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 3).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 7).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel7_entity8() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 1).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 2).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 3).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 7).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 8).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity8_rel7_entity3_rel2_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 8).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 7).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 3).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 2).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 1).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_entity3() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 1).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 3).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity3_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 3).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 1).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 2).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_rel2_entity6() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 1).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 2).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 6).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity6_rel2_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 6).get()),
                new RelationOp(AsgQueryUtils.<Start, Rel>getNextDescendant(asgQuery.getStart(), 2).get()),
                new EntityOp(AsgQueryUtils.<Start, EEntityBase>getNextDescendant(asgQuery.getStart(), 1).get())
        );

        PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
                new CompositePlanOpValidator(Arrays.asList(
                        new AdjacentPlanOpValidator()
                ), CompositePlanOpValidator.Mode.one));

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }
}
