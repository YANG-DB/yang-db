package com.kayhut.fuse.model.execution.plan;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.planTree.PlanNode;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.ETyped;
import org.junit.Test;

import javax.xml.bind.SchemaOutputResolver;
import java.net.SocketPermission;

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

        PlanNode<Plan> planNode = new PlanNode<>(planParent, planParent.toString(), "0", null);
        PlanNode<Plan> cpn1 = new PlanNode<>(childPlan1, childPlan1.toString(), "1", null);
        PlanNode<Plan> cpn2 = new PlanNode<>(childPlan2, childPlan2.toString(), "2", "not valid because blah");
        PlanNode<Plan> cpn3 = new PlanNode<>(childPlan3, childPlan3.toString(), "3", "blah");
        PlanNode<Plan> cpn4 = new PlanNode<>(childPlan4, childPlan4.toString(), "4", "sasaa");

        cpn1.getChildren().add(cpn3);
        cpn2.getChildren().add(cpn4);
        planNode.getChildren().add(cpn1);
        planNode.getChildren().add(cpn2);

        String planTreeJson = new ObjectMapper().writeValueAsString(planNode);


        System.out.println(planTreeJson);
    }
}
