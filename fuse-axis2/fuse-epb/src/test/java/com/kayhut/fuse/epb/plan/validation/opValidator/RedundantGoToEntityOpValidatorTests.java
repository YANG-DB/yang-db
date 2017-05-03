package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Roman on 30/04/2017.
 */
public class RedundantGoToEntityOpValidatorTests {
    //region Valid Plan Tests
    @Test
    public void testValidPlan_entity1_goto1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_goto1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new GoToEntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_goto3() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new GoToEntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel5_entity6_goto1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 6).get()),
                new GoToEntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel5_entity6_goto3() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 6).get()),
                new GoToEntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel5_entity6_goto6() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 6).get()),
                new GoToEntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 6).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }
    //endregion

    //region Invalid Plan Tests
    @Test
    public void testInvalidPlan_entity1_goto3() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }
    //endregion

    //region Fields
    private PlanValidator<Plan, AsgQuery> validator = new ChainedPlanValidator(
            new CompositePlanOpValidator(Arrays.asList(
                    new RedundantGoToEntityOpValidator()
            ), CompositePlanOpValidator.Mode.all));

    //endregion
}
