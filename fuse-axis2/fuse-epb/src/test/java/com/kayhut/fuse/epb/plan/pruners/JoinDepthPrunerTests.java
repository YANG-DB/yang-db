package com.kayhut.fuse.epb.plan.pruners;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static com.kayhut.fuse.model.OntologyTestUtils.OWN;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.rel;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.typed;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

public class JoinDepthPrunerTests {
    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, OntologyTestUtils.DRAGON.type,"B")).build();
    }

    @Test
    public void testTooDeepPlan(){
        AsgQuery query = simpleQuery1("q","o");

        Plan plan = new Plan(new EntityJoinOp(
                new Plan(
                        new EntityJoinOp(
                                new Plan(
                                        new EntityJoinOp(new Plan(new EntityOp(AsgQueryUtil.element$(query, 1))),
                                                            new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)))))
                                ,new Plan(new EntityOp(AsgQueryUtil.element$(query, 1))))),
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)))
        ));

        JoinDepthPruner pruner = new JoinDepthPruner();
        Iterable<Plan> plans = pruner.prunePlans(Collections.singleton(plan));
        Assert.assertEquals(0, Stream.ofAll(plans).length());
    }

    @Test
    public void testValidPlan(){
        AsgQuery query = simpleQuery1("q","o");
        Plan plan = new Plan(new EntityJoinOp(
                new Plan(
                        new EntityJoinOp(new Plan(new EntityOp(AsgQueryUtil.element$(query, 1))),new Plan(new EntityOp(AsgQueryUtil.element$(query, 1))))),
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)))
        ));

        JoinDepthPruner pruner = new JoinDepthPruner();
        Iterable<Plan> plans = pruner.prunePlans(Collections.singleton(plan));
        Assert.assertEquals(1, Stream.ofAll(plans).length());
        Assert.assertEquals(plan, plans.iterator().next());
    }


}
