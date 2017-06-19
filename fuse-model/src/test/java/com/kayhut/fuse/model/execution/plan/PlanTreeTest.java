package com.kayhut.fuse.model.execution.plan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.ETyped;
import org.junit.Test;

/**
 * Created by Roman on 19/06/2017.
 */
public class PlanTreeTest {
    @Test
    public void test1() throws JsonProcessingException {
        Plan planParent = new Plan(
                new EntityOp(new AsgEBase<>(new ETyped(1, "A", 1, 2, 0)))
        );

        Plan childPlan1 = new Plan(
                new EntityOp(new AsgEBase<>(new ETyped(1, "A", 1, 2, 0))),
                new RelationOp(new AsgEBase<>(new Rel(2, 2, Rel.Direction.R, null, 3, 0))),
                new EntityOp(new AsgEBase<>(new ETyped(3, "B", 3, 0, 0)))
        );

        Plan childPlan2 = new Plan(
                new EntityOp(new AsgEBase<>(new ETyped(1, "A", 1, 2, 0))),
                new RelationOp(new AsgEBase<>(new Rel(4, 4, Rel.Direction.L, null, 5, 0))),
                new EntityOp(new AsgEBase<>(new ETyped(5, "C", 5, 0, 0)))
        );

        PlanNode<Plan> planNode = new PlanNode<>(planParent, planParent.toString(), null);
        planNode.getChildren().add(new PlanNode<>(childPlan1, childPlan1.toString(), null));
        planNode.getChildren().add(new PlanNode<>(childPlan2, childPlan2.toString(), "not valid because blah"));

        String planTreeJson = new ObjectMapper().writeValueAsString(planNode);
        int x = 5;
    }
}
