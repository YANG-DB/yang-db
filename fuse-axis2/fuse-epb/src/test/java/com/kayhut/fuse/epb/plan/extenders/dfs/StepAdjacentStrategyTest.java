package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 23/04/2017.
 */
public class StepAdjacentStrategyTest {
    @Test
    public void test_startXeTypedXrelXeTypedXXX_seedPlan() {
        AsgQuery asgQuery = startXeTypedXrelXeTypedXXX_query();
        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)));

        Plan plan = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_startXeTypedXrelXeTypedXXX_fullPlan() {
        AsgQuery asgQuery = startXeTypedXrelXeTypedXXX_query();
        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)));

        List<Plan> extendedPlans = Stream.ofAll(new StepAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 0);
    }

    @Test
    public void test_startXeTypedXrelXeTypedXQuant1XrelXunTypedX_relXconcreteXXXXXX_seedPlan() {
        AsgQuery asgQuery = startXeTypedXrelXeTypedXQuant1XrelXunTypedX_relXconcreteXXXXXX_query();
        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)));

        Plan plan = new Plan(new EntityOp(getAsgEBaseByEnum(asgQuery, 1)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        assertEquals(expectedPlan, actualPlan);
    }

    @Test

    public void test_startXeTypedXrelXeTypedXQuant1XrelXunTypedX_relXconcreteXXXXXX_secondPlan() {
        AsgQuery asgQuery = startXeTypedXrelXeTypedXQuant1XrelXunTypedX_relXconcreteXXXXXX_query();

        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)));

        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)));

        List<Plan> extendedPlans = Stream.ofAll(new StepAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_startXeTypedXrelXeTypedXQuant1XrelXunTypedX_relXconcreteXXXXXX_thirdPlan() {
        AsgQuery asgQuery = startXeTypedXrelXeTypedXQuant1XrelXunTypedX_relXconcreteXXXXXX_query();

        Plan expectedPlan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 7)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 8)));

        Plan plan = new Plan(
                new EntityOp(getAsgEBaseByEnum(asgQuery, 1)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 2)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 3)),
                new RelationOp(getAsgEBaseByEnum(asgQuery, 5)),
                new EntityOp(getAsgEBaseByEnum(asgQuery, 6)));

        List<Plan> extendedPlans = Stream.ofAll(new StepAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        assertEquals(expectedPlan, actualPlan);
    }

    //region Private Methods
    private AsgQuery startXeTypedXrelXeTypedXXX_query() {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType(1);

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(Rel.Direction.R);
        rel.setrType(1);

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(3);
        eTyped2.seteTag("B");
        eTyped2.seteType(2);

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(rel)
                                        .withNext(AsgEBase.Builder.get().withEBase(eTyped2)
                                                .build())
                                        .build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName("name").withOnt("ont").withStart(asgStart).build();
    }

    private AsgQuery startXeTypedXrelXeTypedXQuant1XrelXunTypedX_relXconcreteXXXXXX_query() {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType(1);

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(Rel.Direction.R);
        rel.setrType(1);

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(3);
        eTyped2.seteTag("B");
        eTyped2.seteType(2);

        Quant1 quant1 = new Quant1();
        quant1.seteNum(4);
        quant1.setqType(QuantType.all);

        Rel rel2 = new Rel();
        rel2.seteNum(5);
        rel2.setDir(Rel.Direction.R);
        rel2.setrType(4);

        EUntyped untyped = new EUntyped();
        untyped.seteNum(6);
        untyped.seteTag("C");

        Rel rel3 = new Rel();
        rel3.seteNum(7);
        rel3.setDir(Rel.Direction.R);
        rel3.setrType(5);

        EConcrete concrete = new EConcrete();
        concrete.seteID("concrete1");
        concrete.seteName("Concrete1");
        concrete.seteType(3);
        concrete.seteNum(8);
        concrete.seteTag("D");

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(rel)
                                        .withNext(AsgEBase.Builder.get().withEBase(eTyped2)
                                                .withNext(AsgEBase.Builder.get().withEBase(quant1)
                                                        .withNext(AsgEBase.Builder.get().withEBase(rel2)
                                                                .withNext(AsgEBase.Builder.get().withEBase(untyped)
                                                                .build())
                                                        .build())
                                                        .withNext(AsgEBase.Builder.get().withEBase(rel3)
                                                                .withNext(AsgEBase.Builder.get().withEBase(concrete)
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName("name").withOnt("ont").withStart(asgStart).build();
    }

    private <T extends EBase> AsgEBase<T> getAsgEBaseByEnum(AsgQuery asgQuery, int eNum) {
        return AsgQueryUtils.<Start, T>getNextDescendant(asgQuery.getStart(), eNum).get();
    }

    private void assertEquals(CompositePlanOpBase expectedCompositePlanOp, CompositePlanOpBase actualCompositePlanOp) {
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
            Assert.assertTrue(expectedPlanOp.getClass().equals(actualPlanOp.getClass()));

            if (expectedPlanOp instanceof EntityOp) {
                assertEquals((EntityOp)expectedPlanOp, (EntityOp)actualPlanOp);
            } else if (expectedPlanOp instanceof RelationOp) {
                assertEquals((RelationOp)expectedPlanOp, (RelationOp)actualPlanOp);
            }
        }
    }

    private void assertEquals(EntityOp expectedEntityOp, EntityOp actualEntityOp) {
        if (expectedEntityOp == null) {
            Assert.assertTrue(actualEntityOp == null);
        }

        Assert.assertTrue(expectedEntityOp != null && actualEntityOp != null);
        Assert.assertTrue(expectedEntityOp.geteNum() == actualEntityOp.geteNum());
        Assert.assertTrue(expectedEntityOp.getEntity().geteBase().geteTag().equals(actualEntityOp.getEntity().geteBase().geteTag()));
        Assert.assertTrue(expectedEntityOp.getEntity().geteBase().geteNum() == actualEntityOp.getEntity().geteBase().geteNum());
        Assert.assertTrue(expectedEntityOp.getEntity().geteBase().getClass().equals(actualEntityOp.getEntity().geteBase().getClass()));

        if (expectedEntityOp.getEntity().geteBase() instanceof EConcrete) {
            assertEquals((EConcrete)expectedEntityOp.getEntity().geteBase(), (EConcrete)actualEntityOp.getEntity().geteBase());
        } else if (expectedEntityOp.getEntity().geteBase() instanceof ETyped) {
            assertEquals((ETyped)expectedEntityOp.getEntity().geteBase(), (ETyped)actualEntityOp.getEntity().geteBase());
        } else if (expectedEntityOp.getEntity().geteBase() instanceof EUntyped) {
            assertEquals((EUntyped)expectedEntityOp.getEntity().geteBase(), (EUntyped)actualEntityOp.getEntity().geteBase());
        }
    }

    private void assertEquals(EConcrete expectedConcrete, EConcrete actualConcrete) {
        if (expectedConcrete == null) {
            Assert.assertTrue(actualConcrete == null);
        }

        Assert.assertTrue(expectedConcrete != null && actualConcrete != null);
        Assert.assertEquals(expectedConcrete.geteID(), actualConcrete.geteID());
        Assert.assertEquals(expectedConcrete.geteName(), actualConcrete.geteName());
        Assert.assertEquals(expectedConcrete.geteTag(), actualConcrete.geteTag());
        Assert.assertTrue(expectedConcrete.geteType() == actualConcrete.geteType());
    }

    private void assertEquals(ETyped expectedTyped, ETyped actualTyped) {
        if (expectedTyped == null) {
            Assert.assertTrue(actualTyped == null);
        }

        Assert.assertTrue(expectedTyped != null && actualTyped != null);
        Assert.assertTrue(expectedTyped.geteType() == actualTyped.geteType());
        Assert.assertEquals(expectedTyped.geteTag(), actualTyped.geteTag());
        Assert.assertTrue(expectedTyped.geteNum() == actualTyped.geteNum());
    }

    private void assertEquals(EUntyped expectedUntyped, EUntyped actualUntyped) {
        if (expectedUntyped == null) {
            Assert.assertTrue(actualUntyped == null);
        }

        Assert.assertTrue(expectedUntyped != null && actualUntyped != null);
        Assert.assertTrue(expectedUntyped.geteTag().equals(actualUntyped.geteTag()));
        Assert.assertTrue(expectedUntyped.geteNum() == actualUntyped.geteNum());
    }

    private void assertEquals(RelationOp expectedRelationOp, RelationOp actualRelationOp) {
        if (expectedRelationOp == null) {
            Assert.assertTrue(actualRelationOp == null);
        }

        Assert.assertTrue(expectedRelationOp != null && actualRelationOp != null);
        Assert.assertTrue(expectedRelationOp.geteNum() == actualRelationOp.geteNum());
        Assert.assertTrue(expectedRelationOp.getRelation().geteNum() == actualRelationOp.getRelation().geteNum());

        assertEquals(expectedRelationOp.getRelation().geteBase(), actualRelationOp.getRelation().geteBase());
    }

    private void assertEquals(Rel expectedRel, Rel actualRel) {
        if (expectedRel == null) {
            Assert.assertTrue(actualRel == null);
        }

        Assert.assertTrue(expectedRel != null && actualRel != null);
        Assert.assertTrue(expectedRel.getrType() == actualRel.getrType());
        Assert.assertEquals(expectedRel.getDir(), actualRel.getDir());
        Assert.assertEquals(expectedRel.getWrapper(), actualRel.getWrapper());
        Assert.assertTrue(expectedRel.geteNum() == actualRel.geteNum());
    }
    //endregion
}
