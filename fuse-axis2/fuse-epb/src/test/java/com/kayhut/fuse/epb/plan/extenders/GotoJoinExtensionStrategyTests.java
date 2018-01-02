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
import com.kayhut.fuse.model.query.Rel;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

import static com.kayhut.fuse.model.OntologyTestUtils.FREEZE;
import static com.kayhut.fuse.model.OntologyTestUtils.OWN;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.eProp;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
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
