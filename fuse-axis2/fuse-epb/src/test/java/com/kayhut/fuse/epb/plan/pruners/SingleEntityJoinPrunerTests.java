package com.kayhut.fuse.epb.plan.pruners;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static com.kayhut.fuse.model.OntologyTestUtils.OWN;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.rel;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.typed;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

public class SingleEntityJoinPrunerTests{

    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, OntologyTestUtils.DRAGON.type,"B")).build();
    }

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, OntologyTestUtils.DRAGON.type,"B"))
                .next(rel(4,OWN.getrType(),R))
                .next(typed(5, OntologyTestUtils.DRAGON.type,"C")).build();
    }

    @Test
    public void invalidPlanTest(){
        AsgQuery query = simpleQuery1("q","ont");
        Plan plan = new Plan(new EntityJoinOp(
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 1))),
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 3)),new RelationOp(AsgQueryUtil.element$(query, 2)),new EntityOp(AsgQueryUtil.element$(query, 1)))
        ));

        SingleEntityJoinPruner pruner = new SingleEntityJoinPruner();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = pruner.prunePlans(Collections.singleton(new PlanWithCost<Plan, PlanDetailedCost>(plan, new PlanDetailedCost())));
        Assert.assertEquals(0, Stream.ofAll(plans).length());
    }

    @Test
    public void validPlanTest(){
        AsgQuery query = simpleQuery2("q","ont");
        Plan plan = new Plan(new EntityJoinOp(
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),new RelationOp(AsgQueryUtil.element$(query, 2)),new EntityOp(AsgQueryUtil.element$(query, 3))),
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 5)),new RelationOp(AsgQueryUtil.element$(query, 4)),new EntityOp(AsgQueryUtil.element$(query, 3)))
        ));

        SingleEntityJoinPruner pruner = new SingleEntityJoinPruner();
        Iterable<PlanWithCost<Plan, PlanDetailedCost>> plans = pruner.prunePlans(Collections.singleton(new PlanWithCost<Plan, PlanDetailedCost>(plan, new PlanDetailedCost())));
        Assert.assertEquals(1, Stream.ofAll(plans).length());
        Assert.assertEquals(plan, plans.iterator().next().getPlan());
    }
}
