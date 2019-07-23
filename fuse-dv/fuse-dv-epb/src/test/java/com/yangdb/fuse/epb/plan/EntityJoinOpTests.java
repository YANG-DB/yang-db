package com.yangdb.fuse.epb.plan;

import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.entity.EntityFilterOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityJoinOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.entity.GoToEntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationFilterOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.query.Rel;
import org.junit.Assert;
import org.junit.Test;

import static com.yangdb.fuse.model.OntologyTestUtils.OWN;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;

public class EntityJoinOpTests {

    public static AsgQuery simpleQuery(){
        return AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, OntologyTestUtils.PERSON.type)).
                next(ePropGroup(2)).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4))).
                next(typed(5, OntologyTestUtils.DRAGON.type)).
                next(ePropGroup(6)).
                next(rel(7, OWN.getrType(), Rel.Direction.R).below(relProp(8))).
                next(typed(9, OntologyTestUtils.DRAGON.type)).
                next(ePropGroup(10)).
                build();
    }

    @Test
    public void testCompletePlan(){
        AsgQuery query = simpleQuery();
        EntityJoinOp joinOp = new EntityJoinOp(
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),
                        new EntityFilterOp(AsgQueryUtil.element$(query, 2)),
                        new RelationOp(AsgQueryUtil.element$(query, 3)),
                        new RelationFilterOp(AsgQueryUtil.element$(query, 4)),
                        new EntityOp(AsgQueryUtil.element$(query, 5)),
                        new EntityFilterOp(AsgQueryUtil.element$(query, 6)))
                , new Plan(new EntityOp(AsgQueryUtil.element$(query, 10)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)),
                new RelationOp(AsgQueryUtil.element$(query, 7)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 8)),
                new EntityOp(AsgQueryUtil.element$(query, 5)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 6))));
        Assert.assertTrue(EntityJoinOp.isComplete(joinOp));
    }

    @Test
    public void testInCompletePlan(){
        AsgQuery query = simpleQuery();
        EntityJoinOp joinOp = new EntityJoinOp(
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)),
                        new EntityFilterOp(AsgQueryUtil.element$(query, 2)),
                        new RelationOp(AsgQueryUtil.element$(query, 3)),
                        new RelationFilterOp(AsgQueryUtil.element$(query, 4)),
                        new EntityOp(AsgQueryUtil.element$(query, 5)),
                        new EntityFilterOp(AsgQueryUtil.element$(query, 6)))
                , new Plan(new EntityOp(AsgQueryUtil.element$(query, 10)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9))));
        Assert.assertFalse(EntityJoinOp.isComplete(joinOp));
    }

    @Test
    public void testCompletePlanGoto(){
        AsgQuery query = simpleQuery();
        EntityJoinOp joinOp = new EntityJoinOp(
                new Plan(new EntityOp(AsgQueryUtil.element$(query, 5)),
                        new EntityFilterOp(AsgQueryUtil.element$(query, 6)),
                        new RelationOp(AsgQueryUtil.element$(query, 3)),
                        new RelationFilterOp(AsgQueryUtil.element$(query, 4)),
                        new EntityOp(AsgQueryUtil.element$(query, 1)),
                        new EntityFilterOp(AsgQueryUtil.element$(query, 2)),
                        new GoToEntityOp(AsgQueryUtil.element$(query, 5)))
                , new Plan(new EntityOp(AsgQueryUtil.element$(query, 10)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)),
                new RelationOp(AsgQueryUtil.element$(query, 7)),
                new RelationFilterOp(AsgQueryUtil.element$(query, 8)),
                new EntityOp(AsgQueryUtil.element$(query, 5)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 6))));
        Assert.assertTrue(EntityJoinOp.isComplete(joinOp));
    }
}
