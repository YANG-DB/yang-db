package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.extenders.StepDescendantsAdjacentStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * Created by Roman on 23/04/2017.
 */
public class StepDescendantsAdjacentStrategyTest {
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
                .next(rel(2, "1", R).below(relProp(10, RelProp.of("2", 10, of(eq, "value2")))))
                .next(typed(3, "2", "B"))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of("1", 9, of(eq, "value1")), EProp.of("3", 9, of(gt, "value3")))
                        , rel(5, "4", R)
                                .next(unTyped(6, "C"))
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of("5", 11, of(eq, "value5")), RelProp.of("4", 11, of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D"))
                )
                .build();
    }

    public static AsgQuery simpleQuery3(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "1", "A"))
                .next(rel(2, "1", R).below(relProp(10, RelProp.of("2", 10, of(eq, "value2")))))
                .next(typed(3, "2", "B"))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of("1", 9, of(eq, "value1")), EProp.of("3", 9, of(gt, "value3")))
                        , rel(5, "4", R)
                                .next(unTyped(6, "C")
                                        .next(rel(12, "4", R)
                                                .next(typed(13, "4", "G"))
                                        )
                                )
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of("5", 11, of(eq, "value5")), RelProp.of("4", 11, of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D")
                                        .next(rel(14, "1", R)
                                                .next(typed(15, "1", "F"))
                                        )
                                )
                )
                .build();
    }

    @Test
    public void test_simpleQueryGotoSeedPlan() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        Plan plan = new Plan(new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 1)));
        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(plan, asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }
    @Test
    public void test_simpleQuery1_seedPlan() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        Plan plan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));
        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(plan, asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test
    public void test_simpleQuery1_fullPlan() {
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(plan, asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 0);
    }

    @Test
    public void test_simpleQuery2_seedPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");
        Plan expectedPlan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)));

        Plan plan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));
        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(plan, asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);
        Plan actualPlan = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan, actualPlan);
    }

    @Test

    public void test_simpleQuery2_secondPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        Plan expectedPlan1 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)));

        Plan expectedPlan2 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 7)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 11)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 8)));


        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)));

        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(plan, asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 2);
        Plan actualPlan1 = extendedPlans.get(0);
        Plan actualPlan2 = extendedPlans.get(1);

        PlanAssert.assertEquals(expectedPlan1, actualPlan1);
        PlanAssert.assertEquals(expectedPlan2, actualPlan2);
    }

    @Test
    public void test_simpleQuery2_thirdPlan() {
        AsgQuery asgQuery = simpleQuery3("name", "ont");

        Plan expectedPlan1 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)));

        Plan expectedPlan2 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 7)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 11)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 8)));

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)));

        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(plan, asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 2);

        Plan actualPlan1 = extendedPlans.get(0);
        Plan actualPlan2 = extendedPlans.get(1);

        PlanAssert.assertEquals(expectedPlan1, actualPlan1);
        PlanAssert.assertEquals(expectedPlan2, actualPlan2);
    }

    @Test
    public void test_simpleQuery3_thirdPlan() {
        AsgQuery asgQuery = simpleQuery3("name", "ont");

        Plan expectedPlan1 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 7)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 8)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 14)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 15)));

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 7)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 8)));

        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(plan, asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);

        Plan actualPlan1 = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan1, actualPlan1);
    }

    @Test
    public void test_simpleQuery4_thirdPlan() {
        AsgQuery asgQuery = simpleQuery3("name", "ont");

        Plan expectedPlan1 = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 12)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 13)));

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)));

        List<Plan> extendedPlans = Stream.ofAll(new StepDescendantsAdjacentStrategy().extendPlan(plan, asgQuery)).toJavaList();

        Assert.assertTrue(extendedPlans.size() == 1);

        Plan actualPlan1 = extendedPlans.get(0);

        PlanAssert.assertEquals(expectedPlan1, actualPlan1);
    }
}
