package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.extenders.M1.M1DfsRedundantPlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanAssert;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
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
 * Created by Roman on 11/25/2017.
 */
public class OptionalBranchExtensionStrategyTest {
    @Test
    public void test_query1_1() {
        AsgQuery asgQuery = query1("name", "ont");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 2)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 6)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 7)));

        List<Plan> extendedPlans = Stream.ofAll(new OptionalBranchExtensionStrategy(new StepAdjacentDfsStrategy()).extendPlan(Optional.of(plan), asgQuery)).toJavaList();

        int x = 5;
    }

    public static AsgQuery query1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "entity1", "A"))
                .next(rel(2, "rel1", R).below(relProp(10, RelProp.of("2", 10, of(eq, "value2")))))
                .next(typed(3, "entity2", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(5, EProp.of("prop1", 5, of(eq, "value1")), EProp.of("prop2", 5, of(gt, "value3"))),
                    rel(6, "rel2", R).next(typed(7, "entity3", "C")),
                    optional(8).next(rel(9, "rel3", R).next(typed(10, "entity4", "D"))),
                    optional(11).next(rel(12, "rel4", R).next(typed(13, "entity4", "E")
                            .next(optional(14).next(rel(15, "rel4", R).next(typed(16, "entity4", "F")))))))
                .build();
    }
}
