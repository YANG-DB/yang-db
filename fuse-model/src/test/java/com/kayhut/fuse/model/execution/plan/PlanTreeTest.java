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

        Plan childPlan3 = new Plan(
                new EntityOp(new AsgEBase<>(new ETyped(1, "A", 1, 2, 0))),
                new RelationOp(new AsgEBase<>(new Rel(2, 2, Rel.Direction.R, null, 3, 0))),
                new EntityOp(new AsgEBase<>(new ETyped(3, "B", 3, 0, 0))),
                new RelationOp(new AsgEBase<>(new Rel(3, 2, Rel.Direction.R, null, 3, 0))),
                new EntityOp(new AsgEBase<>(new ETyped(5, "B", 3, 0, 0)))
        );

        Plan childPlan4 = new Plan(
                new EntityOp(new AsgEBase<>(new ETyped(1, "A", 1, 2, 0))),
                new RelationOp(new AsgEBase<>(new Rel(4, 4, Rel.Direction.L, null, 5, 0))),
                new EntityOp(new AsgEBase<>(new ETyped(5, "C", 5, 0, 0))),
                new RelationOp(new AsgEBase<>(new Rel(4, 4, Rel.Direction.L, null, 5, 0))),
                new EntityOp(new AsgEBase<>(new ETyped(5, "C", 5, 0, 0)))
        );

        PlanNode<Plan> planNode = new PlanNode<>(0,planParent.toPattern(),"",planParent.hashCode()+"" , "valid");
        PlanNode<Plan> cpn1 = new PlanNode<>(1 ,childPlan1.toPattern(),"", childPlan1.hashCode()+"",  "valid");
        PlanNode<Plan> cpn2 = new PlanNode<>(2,childPlan2.toPattern(),"",  childPlan2.hashCode()+"","not valid because blah");
        PlanNode<Plan> cpn3 = new PlanNode<>(3, childPlan3.toPattern(),"", childPlan3.hashCode()+"",  "blah");
        PlanNode<Plan> cpn4 = new PlanNode<>(4,childPlan4.toPattern(), "",childPlan4.hashCode()+"",  "sasaa");

        cpn1.getChildren().add(cpn3);
        cpn2.getChildren().add(cpn4);
        planNode.getChildren().add(cpn1);
        planNode.getChildren().add(cpn2);

        String planTreeJson = new ObjectMapper().writeValueAsString(planNode);


        System.out.println(planTreeJson);
    }
}
