package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Level;

import static com.kayhut.fuse.epb.tests.PlanMockUtils.PlanMockBuilder.mock;

/**
 * Created by Roman on 04/05/2017.
 */
public class M1PlanValidatorTests {
    //region Valid Plan Tests
    @Test
    public void testValidPlan_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()))
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_rel2_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_filter9() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtils.<EPropGroup>getElement(asgQuery, 9).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel5() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_filter10_entity3_filter9_rel5() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtils.<RelPropGroup>getElement(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtils.<EPropGroup>getElement(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_rel2_entity1_goto3_rel5() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get())
        );

        validator.getLogs(Level.INFO).forEach(p-> System.out.println(p._1+":"+p._2));
        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_filter9_rel2_filter10_entity1_goto3_rel5() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtils.<EPropGroup>getElement(asgQuery, 9).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())),
                new RelationFilterOp(AsgQueryUtils.<RelPropGroup>getElement(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get())
        );

        validator.getLogs(Level.INFO).forEach(p-> System.out.println(p._1+":"+p._2));
        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity3_rel2_entity1_goto3_rel5_entity6() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 6).get())
        );
        boolean planValid = validator.isPlanValid(plan, asgQuery);
        validator.getLogs(Level.INFO).forEach(p-> System.out.println(p._1+":"+p._2));
        Assert.assertTrue(planValid);
    }

    @Test
    public void testValidPlan_entity3_filter9_rel2_filter10_entity1_goto3_rel5_entity6() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtils.<EPropGroup>getElement(asgQuery, 9).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())),
                new RelationFilterOp(AsgQueryUtils.<RelPropGroup>getElement(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 6).get())
        );

        boolean planValid = validator.isPlanValid(plan, asgQuery);
        validator.getLogs(Level.INFO).forEach(p-> System.out.println(p._1+":"+p._2));
        Assert.assertTrue(planValid);
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel5_entity6() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 6).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_filter10_entity3_filter9_rel5_entity6() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtils.<RelPropGroup>getElement(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtils.<EPropGroup>getElement(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 6).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity6_rel5_entity3_rel2_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 6).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get())),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity6_rel5_entity3_filter9_rel2_filter10_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 6).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get())),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtils.<EPropGroup>getElement(asgQuery, 9).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())),
                new RelationFilterOp(AsgQueryUtils.<RelPropGroup>getElement(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel7() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 7).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_filter10_entity3_filter9_rel7() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtils.<RelPropGroup>getElement(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtils.<EPropGroup>getElement(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 7).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_entity3_rel7_entity8() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 7).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 8).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity1_rel2_filter10_entity3_filter9_rel7_filter11_entity8() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new RelationFilterOp(AsgQueryUtils.<RelPropGroup>getElement(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtils.<EPropGroup>getElement(asgQuery, 9).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 7).get()),
                new RelationFilterOp(AsgQueryUtils.<RelPropGroup>getElement(asgQuery, 11).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 8).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlanEntityOp_1_RelationOp_2_RelationFilterOp_10_EntityOp_3_EntityFilterOp_9_RelationOp_7_RelationFilterOp_11_EntityOp_8_GoToEntityOp_3_RelationOp_5_EntityOp_6() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = mock(asgQuery).entity(1).rel(2).relFilter(10).entity(3).entityFilter(9).rel(7).relFilter(11).entity(8).goTo(3).rel(5).entity(6).plan();
        boolean planValid = validator.isPlanValid(plan, asgQuery);
        validator.getLogs(Level.INFO).forEach(p-> System.out.println(p._1+":"+p._2));
        Assert.assertTrue(planValid);
    }
    @Test
    public void testValidPlanEntityOp_3_EntityFilterOp_9_RelationOp_7_RelationFilterOp_11_EntityOp_8_GoToEntityOp_3_RelationOp_5_EntityOp_6_GoToEntityOp_3_RelationOp_2_RelationFilterOp_10_EntityOp_1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = mock(asgQuery).entity(3).entityFilter(9).rel(7).relFilter(11).entity(8).goTo(3).rel(5).entity(6).goTo(3).rel(2,Rel.Direction.L).relFilter(10).entity(1).plan();
        boolean planValid = validator.isPlanValid(plan, asgQuery);
        validator.getLogs(Level.INFO).forEach(p-> System.out.println(p._1+":"+p._2));
        Assert.assertTrue(planValid);
    }

    @Test
    public void testValidPlan_entity8_rel7_entity3_rel2_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 8).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 7).get())),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testValidPlan_entity8_rel7_filter11_entity3_filter9_rel2_filter10_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 8).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 7).get())),
                new RelationFilterOp(AsgQueryUtils.<RelPropGroup>getElement(asgQuery, 11).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new EntityFilterOp(AsgQueryUtils.<EPropGroup>getElement(asgQuery, 9).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())),
                new RelationFilterOp(AsgQueryUtils.<RelPropGroup>getElement(asgQuery, 10).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get())
        );

        Assert.assertTrue(validator.isPlanValid(plan, asgQuery));
    }
    //endregion

    //region Invalid Plan Tests
    @Test
    public void testInvalidPlan_entity1_entity3() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity3_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_rel2_entity6() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 6).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity6_rel2_entity1() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 6).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_filter9() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new EntityFilterOp(AsgQueryUtils.<EPropGroup>getElement(asgQuery, 9).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_filter10() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationFilterOp(AsgQueryUtils.<RelPropGroup>getElement(asgQuery, 10).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_rel2_filter9() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityFilterOp(AsgQueryUtils.<EPropGroup>getElement(asgQuery, 9).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_filter9_filter10() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new EntityFilterOp(AsgQueryUtils.<EPropGroup>getElement(asgQuery, 9).get()),
                new RelationFilterOp(AsgQueryUtils.<RelPropGroup>getElement(asgQuery, 10).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_rel2_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_rel2_entity3_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_rel2_entity3_rel5_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_rel2_entity3_rel5_rel5() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity6_rel5_entity3_rel5() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 6).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity6_rel5_entity3_rel2_rel5() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 6).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get()),
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 5).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_goto3() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new GoToEntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_rel2_goto3() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()),
                new GoToEntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity3_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 3).get()),
                new RelationOp(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get())
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }

    @Test
    public void testInvalidPlan_entity1_rel2() {
        AsgQuery asgQuery = AsgQueryStore.simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtils.<EEntityBase>getElement(asgQuery, 1).get()),
                new RelationOp(reverseRelation(AsgQueryUtils.<Rel>getElement(asgQuery, 2).get()))
        );

        Assert.assertFalse(validator.isPlanValid(plan, asgQuery));
    }
    //endregion

    //region Private Methods
    private AsgEBase<Rel> reverseRelation(AsgEBase<Rel> relAsgEBase) {
        Rel reversedRel = new Rel();
        reversedRel.seteNum(relAsgEBase.geteNum());
        reversedRel.setrType(relAsgEBase.geteBase().getrType());
        reversedRel.setDir(relAsgEBase.geteBase().getDir() == Rel.Direction.L ? Rel.Direction.R : Rel.Direction.L);

        return AsgEBase.Builder.<Rel>get().withEBase(reversedRel).build();
    }
    //endregion

    //region Fields
    private PlanValidator<Plan, AsgQuery> validator = new M1PlanValidator();
    //endregion
}
