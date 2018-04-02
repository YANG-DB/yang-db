package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.entity.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.EProp;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.eProp;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.*;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.le;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.RelProp.of;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

public class GotoJoinExtensionStrategyTests {
    public static AsgQuery lineQuery() {
        return AsgQuery.Builder.start("q", "o")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(eProp(2))
                .next(rel(3, OWN.getrType(), R).below(relProp(4)))
                .next(typed(5, OntologyTestUtils.DRAGON.type))
                .next(eProp(6))
                .next(rel(7, FREEZE.getrType(), R).below(relProp(8)))
                .next(unTyped(9))
                .next(eProp(10))
                .next(rel(11, FREEZE.getrType(), R).below(relProp(12)))
                .next(unTyped(13))
                .next(eProp(14))
                .build();
    }

    public static AsgQuery starQuery(){
        return AsgQuery.Builder.start("q", "o")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(quant1(2, all))
                .in(
                        eProp(3),
                        rel(4, OWN.getrType(), R).below(relProp(5))
                        .next(typed(6, OntologyTestUtils.DRAGON.type))
                        .next(eProp(7)),
                        rel(8, FREEZE.getrType(), R).below(relProp(9))
                        .next(unTyped(10))
                        .next(eProp(11)),
                        rel(12, FREEZE.getrType(), R).below(relProp(13))
                        .next(unTyped(14))
                        .next(eProp(15)),
                        rel(16, FREEZE.getrType(), R).below(relProp(17))
                                .next(unTyped(18))
                                .next(eProp(19))
                        )
                .build();
    }
    public static AsgQuery simpleQuery3(String queryName, String ontologyName) {
        long time = System.currentTimeMillis();
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(rel(2, OWN.getrType(), R).below(relProp(10, of(10, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.DRAGON.type))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of(9, NAME.type, Constraint.of(eq, "Moshe")), EProp.of(9, GENDER.type, Constraint.of(gt, MALE)))
                        , rel(5, FIRE.getrType(), R)
                                .next(unTyped(6)
                                        .next(rel(12, REGISTERED.getrType(), R)
                                                .next(typed(13, KINGDOM.type))
                                        )
                                )
                        , rel(7, FREEZE.getrType(), R)
                                .below(relProp(11, of(11, START_DATE.type,
                                        Constraint.of(ge, new Date(time - 1000 * 60))),
                                        of(11, END_DATE.type, Constraint.of(le, new Date(time + 1000 * 60)))))
                                .next(concrete(8, "Beltazar", DRAGON.type, "Beltazar", "D")
                                        .next(rel(14, ORIGIN.getrType(), R)
                                                .next(typed(15, KINGDOM.type))
                                        )
                                )
                ).build();
    }

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        long time = System.currentTimeMillis();
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(rel(2, OWN.getrType(), R).below(relProp(10, of(10, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.DRAGON.type))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of(9, NAME.type, Constraint.of(eq, "smith")), EProp.of(9, GENDER.type, Constraint.of(gt, MALE)))
                        , rel(5, FREEZE.getrType(), R)
                                .next(unTyped(6))
                        , rel(7, FIRE.getrType(), R)
                                .below(relProp(11, of(11, START_DATE.type,
                                        Constraint.of(ge, new Date(time - 1000 * 60))),
                                        of(11, END_DATE.type, Constraint.of(le, new Date(time + 1000 * 60)))))
                                .next(concrete(8, "smoge", DRAGON.type, "Display:smoge", "D"))
                )
                .build();
    }


    @Test
    public void test_simpleQuery2_secondPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)));

        List<Plan> extendedPlans = Stream.ofAll(new GotoJoinExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();
        Assert.assertTrue(extendedPlans.size() == 1);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(0).getOps().size() - 1) instanceof GoToEntityOp);
    }

    @Test
    public void test_complexQuery2_secondPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");

        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)),
                new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 7)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 11)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 8)));

        List<Plan> extendedPlans = Stream.ofAll(new GotoJoinExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();
        Assert.assertTrue(extendedPlans.size() == 3);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(0).getOps().size() - 1) instanceof GoToEntityOp);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(1).getOps().size() - 1) instanceof GoToEntityOp);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(2).getOps().size() - 1) instanceof GoToEntityOp);
    }

    @Test
    public void test_simpleQuery2_thirdPlan() {
        AsgQuery asgQuery = simpleQuery2("name", "ont");


        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)));

        List<Plan> extendedPlans = Stream.ofAll(new GotoJoinExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();
        Assert.assertTrue(extendedPlans.size() == 2);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(0).getOps().size() - 1) instanceof GoToEntityOp);
        Assert.assertEquals(extendedPlans.get(0).getOps().size(), 8);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(1).getOps().size() - 1) instanceof GoToEntityOp);
        Assert.assertEquals(extendedPlans.get(1).getOps().size(), 8);
    }

    @Test
    public void test_simpleQuery3_thirdPlan() {
        AsgQuery asgQuery = simpleQuery3("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 6)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 12)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 13)));

        List<Plan> extendedPlans = Stream.ofAll(new GotoJoinExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();
        Assert.assertTrue(extendedPlans.size() == 3);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(0).getOps().size() - 1) instanceof GoToEntityOp);
        Assert.assertEquals(extendedPlans.get(0).getOps().size(), 10);
        Assert.assertTrue(extendedPlans.get(1).getOps().get(extendedPlans.get(1).getOps().size() - 1) instanceof GoToEntityOp);
        Assert.assertEquals(extendedPlans.get(1).getOps().size(), 10);
        Assert.assertTrue(extendedPlans.get(2).getOps().get(extendedPlans.get(2).getOps().size() - 1) instanceof GoToEntityOp);
        Assert.assertEquals(extendedPlans.get(2).getOps().size(), 10);
    }
    @Test
    public void testJoinGotoLineQuery(){
        AsgQuery asgQuery = lineQuery();
        Plan plan = new Plan(new EntityJoinOp(
                new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                        new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 2)),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 3)),
                        new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 4)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 5)),
                        new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 6))),
                new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 9)),
                        new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 10)),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 7)),
                        new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 8)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 5)),
                        new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 6)))));
        Iterable<Plan> plans = new GotoJoinExtensionStrategy().extendPlan(Optional.of(plan), asgQuery);
        Assert.assertEquals(2, Stream.ofAll(plans).length());
        Assert.assertTrue(Stream.ofAll(plans).exists(p -> {
            PlanOp planOp = p.getOps().get(p.getOps().size() - 1);
            return planOp instanceof GoToEntityOp && ((GoToEntityOp) planOp).getAsgEbase().geteNum() == 1;
        }));
        Assert.assertTrue(Stream.ofAll(plans).exists(p -> {
            PlanOp planOp = p.getOps().get(p.getOps().size() - 1);
            return planOp instanceof GoToEntityOp && ((GoToEntityOp) planOp).getAsgEbase().geteNum() == 9;
        }));
    }

    @Test
    public void testJoinGotoStarQuery(){
        AsgQuery asgQuery = starQuery();
        Plan plan = new Plan(new EntityJoinOp(
                new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                        new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 3)),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 4)),
                        new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 5)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 6)),
                        new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 7))),
                new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 10)),
                        new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 11)),
                        new RelationOp(AsgQueryUtil.element$(asgQuery, 8), Rel.Direction.L),
                        new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 9)),
                        new EntityOp(AsgQueryUtil.element$(asgQuery, 6)),
                        new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 7)))),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 12)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 13)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 14)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 15))
                );
        Iterable<Plan> plans = new GotoJoinExtensionStrategy().extendPlan(Optional.of(plan), asgQuery);
        Assert.assertEquals(3, Stream.ofAll(plans).length());
        Assert.assertTrue(Stream.ofAll(plans).exists(p -> {
            PlanOp planOp = p.getOps().get(p.getOps().size() - 1);
            return planOp instanceof GoToEntityOp && ((GoToEntityOp) planOp).getAsgEbase().geteNum() == 1;
        }));
        Assert.assertTrue(Stream.ofAll(plans).exists(p -> {
            PlanOp planOp = p.getOps().get(p.getOps().size() - 1);
            return planOp instanceof GoToEntityOp && ((GoToEntityOp) planOp).getAsgEbase().geteNum() == 6;
        }));
        Assert.assertTrue(Stream.ofAll(plans).exists(p -> {
            PlanOp planOp = p.getOps().get(p.getOps().size() - 1);
            return planOp instanceof GoToEntityOp && ((GoToEntityOp) planOp).getAsgEbase().geteNum() == 10;
        }));
    }
}
