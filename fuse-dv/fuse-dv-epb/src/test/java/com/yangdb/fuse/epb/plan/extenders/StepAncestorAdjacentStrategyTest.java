package com.yangdb.fuse.epb.plan.extenders;

import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.*;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.entity.EntityFilterOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.entity.GoToEntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationFilterOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.RelProp;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.execution.plan.Direction.reverse;
import static com.yangdb.fuse.model.query.properties.constraint.Constraint.of;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.gt;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static com.yangdb.fuse.model.query.quant.QuantType.all;

/**
 * Created by Roman on 23/04/2017.
 */
public class StepAncestorAdjacentStrategyTest {
    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("1");

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(R);
        rel.setrType("1");

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(3);
        eTyped2.seteTag("B");
        eTyped2.seteType("2");

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(rel)
                                        .withNext(AsgEBase.Builder.get().withEBase(eTyped2)
                                                .build())
                                        .build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "1", "A"))
                .next(rel(2, "1", R).below(relProp(10, RelProp.of(10, "2", of(eq, "value2")))))
                .next(typed(3, "2", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(9, EProp.of(9, "1", of(eq, "value1")), EProp.of(9, "3", of(gt, "value3")))
                        , rel(5, "4", R)
                                .next(unTyped(6, "C"))
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of(11, "5", of(eq, "value5")), RelProp.of(11, "4", of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D"))
                )
                .build();
    }

    @Test
    public void test_simpleQuery1_seedPlan() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 0);
    }

    @Test
    public void test_simpleQuery1_fullPlan() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        AsgEBase<Rel> relation = AsgQueryUtil.element$(asgQuery, 2);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));

        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 0);
    }

    @Test
    public void test_simpleQuery2_fullPlan() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        AsgEBase<Rel> relation = AsgQueryUtil.element$(asgQuery, 2);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));
        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2), R),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 3)))), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_simpleQuery2ReversefullPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));


        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        AsgEBase<Rel> relation = AsgQueryUtil.element$(asgQuery, 2);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));

        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_simpleQuery2_seedPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        AsgEBase<Rel> relation = AsgQueryUtil.element$(asgQuery, 5);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));
        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5),R),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)));

        Plan plan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 6)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_simpleQuery2GotoPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        AsgEBase<Rel> relation = AsgQueryUtil.element$(asgQuery, 5);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));
        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5), R),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)));

        Plan plan = new Plan(new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 6)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_simpleQuery2_secondPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        AsgEBase<Rel> relation = AsgQueryUtil.element$(asgQuery, 7);
        relation.geteBase().setDir(reverse(relation.geteBase().getDir()));

        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 8)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 7), R),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 11)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)));
        Plan plan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 8)));
        List<Plan> extendedPlans = Stream.ofAll(new StepAncestorAdjacentStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }
}
