package com.kayhut.fuse.epb.plan.extenders.dfs;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.extenders.GotoExtensionStrategy;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.END_DATE;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.ConstraintOp.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.RelProp.of;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

public class GotoGeneratorExtenderStrategyTest {
    public static AsgQuery simpleQuery3(String queryName, String ontologyName) {
        long time = System.currentTimeMillis();
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, PERSON.type))
                .next(rel(2, OWN.getrType(), R).below(relProp(10, of(START_DATE.type, 10, Constraint.of(eq, new Date())))))
                .next(typed(3, DRAGON.type))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of(NAME.type, 9, Constraint.of(eq, "Moshe")), EProp.of(GENDER.type, 9, Constraint.of(gt, MALE)))
                        , rel(5, FIRE.getrType(), R)
                                .next(unTyped(6)
                                        .next(rel(12, REGISTERED.getrType(), R)
                                                .next(typed(13, KINGDOM.type))
                                        )
                                )
                        , rel(7, FREEZE.getrType(), R)
                                .below(relProp(11, of(START_DATE.type, 11,
                                        Constraint.of(ge, new Date(time - 1000 * 60))),
                                        of(END_DATE.type, 11, Constraint.of(le, new Date(time + 1000 * 60)))))
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
                .next(rel(2, OWN.getrType(), R).below(relProp(10, of(START_DATE.type, 10, Constraint.of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.DRAGON.type))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of(NAME.type, 9, Constraint.of(eq, "smith")), EProp.of(GENDER.type, 9, Constraint.of(gt, MALE)))
                        , rel(5, FREEZE.getrType(), R)
                                .next(unTyped(6))
                        , rel(7, FIRE.getrType(), R)
                                .below(relProp(11, of(START_DATE.type, 11,
                                        Constraint.of(ge, new Date(time - 1000 * 60))),
                                        of(END_DATE.type, 11, Constraint.of(le, new Date(time + 1000 * 60)))))
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

        List<Plan> extendedPlans = Stream.ofAll(new GotoExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();
        Assert.assertTrue(extendedPlans.size() == 1);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(0).getOps().size()-1) instanceof GoToEntityOp);
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

        List<Plan> extendedPlans = Stream.ofAll(new GotoExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();
        Assert.assertTrue(extendedPlans.size() == 3);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(0).getOps().size()-1) instanceof GoToEntityOp);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(1).getOps().size()-1) instanceof GoToEntityOp);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(2).getOps().size()-1) instanceof GoToEntityOp);
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

        List<Plan> extendedPlans = Stream.ofAll(new GotoExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();
        Assert.assertTrue(extendedPlans.size() == 2);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(0).getOps().size()-1) instanceof GoToEntityOp);
        Assert.assertEquals(extendedPlans.get(0).getOps().size(),8);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(1).getOps().size()-1) instanceof GoToEntityOp);
        Assert.assertEquals(extendedPlans.get(1).getOps().size(),8);
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

        List<Plan> extendedPlans = Stream.ofAll(new GotoExtensionStrategy().extendPlan(Optional.of(plan), asgQuery)).toJavaList();
        Assert.assertTrue(extendedPlans.size() == 3);
        Assert.assertTrue(extendedPlans.get(0).getOps().get(extendedPlans.get(0).getOps().size()-1) instanceof GoToEntityOp);
        Assert.assertEquals(extendedPlans.get(0).getOps().size(),10);
        Assert.assertTrue(extendedPlans.get(1).getOps().get(extendedPlans.get(1).getOps().size()-1) instanceof GoToEntityOp);
        Assert.assertEquals(extendedPlans.get(1).getOps().size(),10);
        Assert.assertTrue(extendedPlans.get(2).getOps().get(extendedPlans.get(2).getOps().size()-1) instanceof GoToEntityOp);
        Assert.assertEquals(extendedPlans.get(2).getOps().size(),10);
    }
}
