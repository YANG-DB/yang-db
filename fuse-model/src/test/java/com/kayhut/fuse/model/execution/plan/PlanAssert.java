package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.query.QueryAssert;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import org.junit.Assert;

/**
 * Created by Roman on 25/04/2017.
 */
public class PlanAssert {
    public static void assertEquals(CompositePlanOpBase expectedCompositePlanOp, CompositePlanOpBase actualCompositePlanOp) {
        if (expectedCompositePlanOp.getOps() == null) {
            Assert.assertTrue(actualCompositePlanOp.getOps() == null);
        }

        Assert.assertTrue(expectedCompositePlanOp.getOps() != null && actualCompositePlanOp.getOps() != null);
        Assert.assertTrue(expectedCompositePlanOp.getOps().size() == actualCompositePlanOp.getOps().size());

        for(int i = 0 ; i < expectedCompositePlanOp.getOps().size() ; i++) {
            PlanOpBase expectedPlanOp = expectedCompositePlanOp.getOps().get(i);
            PlanOpBase actualPlanOp = actualCompositePlanOp.getOps().get(i);

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
        Assert.assertTrue(expectedEntityOp.geteNum() == actualEntityOp.geteNum());
        Assert.assertTrue(expectedEntityOp.getAsgEBase().geteBase().geteTag().equals(actualEntityOp.getAsgEBase().geteBase().geteTag()));
        Assert.assertTrue(expectedEntityOp.getAsgEBase().geteBase().geteNum() == actualEntityOp.getAsgEBase().geteBase().geteNum());
        Assert.assertTrue(expectedEntityOp.getAsgEBase().geteBase().getClass().equals(actualEntityOp.getAsgEBase().geteBase().getClass()));

        if (expectedEntityOp.getAsgEBase().geteBase() instanceof EConcrete) {
            QueryAssert.assertEquals((EConcrete)expectedEntityOp.getAsgEBase().geteBase(), (EConcrete)actualEntityOp.getAsgEBase().geteBase());
        } else if (expectedEntityOp.getAsgEBase().geteBase() instanceof ETyped) {
            QueryAssert.assertEquals((ETyped)expectedEntityOp.getAsgEBase().geteBase(), (ETyped)actualEntityOp.getAsgEBase().geteBase());
        } else if (expectedEntityOp.getAsgEBase().geteBase() instanceof EUntyped) {
            QueryAssert.assertEquals((EUntyped)expectedEntityOp.getAsgEBase().geteBase(), (EUntyped)actualEntityOp.getAsgEBase().geteBase());
        }
    }

    public static void assertEquals(RelationOp expectedRelationOp, RelationOp actualRelationOp) {
        if (expectedRelationOp == null) {
            Assert.assertTrue(actualRelationOp == null);
        }

        Assert.assertTrue(expectedRelationOp != null && actualRelationOp != null);
        Assert.assertTrue(expectedRelationOp.geteNum() == actualRelationOp.geteNum());
        Assert.assertTrue(expectedRelationOp.getAsgEBase().geteNum() == actualRelationOp.getAsgEBase().geteNum());

        QueryAssert.assertEquals(expectedRelationOp.getAsgEBase().geteBase(), actualRelationOp.getAsgEBase().geteBase());
    }
}
