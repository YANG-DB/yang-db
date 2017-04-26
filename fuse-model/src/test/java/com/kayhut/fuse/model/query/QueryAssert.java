package com.kayhut.fuse.model.query;

import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.quant.Quant1;
import org.junit.Assert;

/**
 * Created by Roman on 25/04/2017.
 */
public class QueryAssert {
    public static void assertEquals(EConcrete expectedConcrete, EConcrete actualConcrete) {
        if (expectedConcrete == null) {
            Assert.assertTrue(actualConcrete == null);
        }

        Assert.assertTrue(expectedConcrete != null && actualConcrete != null);
        Assert.assertEquals(expectedConcrete.geteID(), actualConcrete.geteID());
        Assert.assertEquals(expectedConcrete.geteName(), actualConcrete.geteName());
        Assert.assertEquals(expectedConcrete.geteTag(), actualConcrete.geteTag());
        Assert.assertTrue(expectedConcrete.geteType() == actualConcrete.geteType());
    }

    public static void assertEquals(ETyped expectedTyped, ETyped actualTyped) {
        if (expectedTyped == null) {
            Assert.assertTrue(actualTyped == null);
        }

        Assert.assertTrue(expectedTyped != null && actualTyped != null);
        Assert.assertTrue(expectedTyped.geteType() == actualTyped.geteType());
        Assert.assertEquals(expectedTyped.geteTag(), actualTyped.geteTag());
        Assert.assertTrue(expectedTyped.geteNum() == actualTyped.geteNum());
    }

    public static void assertEquals(EUntyped expectedUntyped, EUntyped actualUntyped) {
        if (expectedUntyped == null) {
            Assert.assertTrue(actualUntyped == null);
        }

        Assert.assertTrue(expectedUntyped != null && actualUntyped != null);
        Assert.assertTrue(expectedUntyped.geteTag().equals(actualUntyped.geteTag()));
        Assert.assertTrue(expectedUntyped.geteNum() == actualUntyped.geteNum());
    }

    public static void assertEquals(Rel expectedRel, Rel actualRel) {
        if (expectedRel == null) {
            Assert.assertTrue(actualRel == null);
        }

        Assert.assertTrue(expectedRel != null && actualRel != null);
        Assert.assertTrue(expectedRel.getrType() == actualRel.getrType());
        Assert.assertEquals(expectedRel.getDir(), actualRel.getDir());
        Assert.assertEquals(expectedRel.getWrapper(), actualRel.getWrapper());
        Assert.assertTrue(expectedRel.geteNum() == actualRel.geteNum());
    }

    public static void assertEquals(Quant1 expectedQuant, Quant1 actualQuant) {
        if (expectedQuant == null) {
            Assert.assertTrue(actualQuant == null);
        }

        Assert.assertTrue(expectedQuant != null && actualQuant != null);
        Assert.assertEquals(expectedQuant.geteNum(), actualQuant.geteNum());
        Assert.assertEquals(expectedQuant.getqType(), actualQuant.getqType());
    }
}
