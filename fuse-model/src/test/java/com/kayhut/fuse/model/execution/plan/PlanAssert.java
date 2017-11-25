package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.query.QueryAssert;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import org.junit.Assert;

/**
 * Created by Roman on 25/04/2017.
 */
public class PlanAssert {
    public static void assertEquals(CompositePlanOp expectedCompositePlanOp, CompositePlanOp actualCompositePlanOp) {
        if (expectedCompositePlanOp == null) {
            Assert.assertTrue(actualCompositePlanOp == null);
        }

        Assert.assertTrue(expectedCompositePlanOp != null && actualCompositePlanOp != null);

        if (expectedCompositePlanOp.getOps() == null) {
            Assert.assertTrue(actualCompositePlanOp.getOps() == null);
        }

        Assert.assertTrue(expectedCompositePlanOp.getOps() != null && actualCompositePlanOp.getOps() != null);
        Assert.assertTrue(expectedCompositePlanOp.getOps().size() == actualCompositePlanOp.getOps().size());

        for(int i = 0 ; i < expectedCompositePlanOp.getOps().size() ; i++) {
            PlanOp expectedPlanOp = expectedCompositePlanOp.getOps().get(i);
            PlanOp actualPlanOp = actualCompositePlanOp.getOps().get(i);

            if (expectedPlanOp == null) {
                Assert.assertTrue(actualPlanOp == null);
            }

            Assert.assertTrue(expectedPlanOp != null && actualPlanOp != null);
            Assert.assertTrue(expectedPlanOp.getClass().isAssignableFrom(actualPlanOp.getClass()));

            if (expectedPlanOp instanceof EntityOp) {
                assertEquals((EntityOp)expectedPlanOp, (EntityOp)actualPlanOp);
            } else if (expectedPlanOp instanceof RelationOp) {
                assertEquals((RelationOp)expectedPlanOp, (RelationOp)actualPlanOp);
            }
        }
    }

    public static void assertEquals(EntityOp expectedEntityOp, EntityOp actualEntityOp) {
        if (expectedEntityOp == null) {
            Assert.assertTrue(actualEntityOp == null);
        }

        Assert.assertTrue(expectedEntityOp != null && actualEntityOp != null);
        Assert.assertTrue(expectedEntityOp.getAsgEbase().geteNum() == actualEntityOp.getAsgEbase().geteNum());
        if(expectedEntityOp.getAsgEbase().geteBase().geteTag() != null) {
            Assert.assertTrue(expectedEntityOp.getAsgEbase().geteBase().geteTag().equals(actualEntityOp.getAsgEbase().geteBase().geteTag()));
        }else{
            Assert.assertTrue(actualEntityOp.getAsgEbase().geteBase().geteTag() == null);
        }
        Assert.assertTrue(expectedEntityOp.getAsgEbase().geteBase().geteNum() == actualEntityOp.getAsgEbase().geteBase().geteNum());
        Assert.assertTrue(expectedEntityOp.getAsgEbase().geteBase().getClass().equals(actualEntityOp.getAsgEbase().geteBase().getClass()));

        if (expectedEntityOp.getAsgEbase().geteBase() instanceof EConcrete) {
            QueryAssert.assertEquals((EConcrete)expectedEntityOp.getAsgEbase().geteBase(), (EConcrete)actualEntityOp.getAsgEbase().geteBase());
        } else if (expectedEntityOp.getAsgEbase().geteBase() instanceof ETyped) {
            QueryAssert.assertEquals((ETyped)expectedEntityOp.getAsgEbase().geteBase(), (ETyped)actualEntityOp.getAsgEbase().geteBase());
        } else if (expectedEntityOp.getAsgEbase().geteBase() instanceof EUntyped) {
            QueryAssert.assertEquals((EUntyped)expectedEntityOp.getAsgEbase().geteBase(), (EUntyped)actualEntityOp.getAsgEbase().geteBase());
        }
    }

    public static void assertEquals(RelationOp expectedRelationOp, RelationOp actualRelationOp) {
        if (expectedRelationOp == null) {
            Assert.assertTrue(actualRelationOp == null);
        }

        Assert.assertTrue(expectedRelationOp != null && actualRelationOp != null);
        Assert.assertTrue(expectedRelationOp.getAsgEbase().geteNum() == actualRelationOp.getAsgEbase().geteNum());
        Assert.assertTrue(expectedRelationOp.getAsgEbase().geteNum() == actualRelationOp.getAsgEbase().geteNum());

        QueryAssert.assertEquals(expectedRelationOp.getAsgEbase().geteBase(), actualRelationOp.getAsgEbase().geteBase());
    }
}
