package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.JoinCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.unipop.process.JoinStep;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.function.Predicate;

public class JoinEntityOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    PlanTraversalTranslator planTraversalTranslator;

    public JoinEntityOpTranslationStrategy(PlanTraversalTranslator planTraversalTranslator, Class<? extends PlanOp>... klasses) {
        super(klasses);
        this.planTraversalTranslator = planTraversalTranslator;
    }

    public JoinEntityOpTranslationStrategy(Predicate<PlanOp> planOpPredicate, PlanTraversalTranslator planTraversalTranslator) {
        super(planOpPredicate);
        this.planTraversalTranslator = planTraversalTranslator;
    }

    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        if(planOp instanceof EntityJoinOp){
            JoinCost joinCost = (JoinCost) plan.getCost().getPlanStepCost(planOp).get().getCost();
            CountEstimatesCost leftCost = Stream.ofAll(joinCost.getLeftBranchCost().getPlanStepCosts()).last().getCost();
            CountEstimatesCost rightCost = Stream.ofAll(joinCost.getRightBranchCost().getPlanStepCosts()).last().getCost();

            traversal.asAdmin().addStep(new JoinStep(traversal.asAdmin()));

            if(leftCost.peek() < rightCost.peek()){
                traversal.by(planTraversalTranslator.translate(new PlanWithCost<>(((EntityJoinOp) planOp).getRightBranch(), joinCost.getRightBranchCost()), context));
                traversal.by(planTraversalTranslator.translate(new PlanWithCost<>(((EntityJoinOp) planOp).getLeftBranch(), joinCost.getLeftBranchCost()), context));

            }else{
                traversal.by(planTraversalTranslator.translate(new PlanWithCost<>(((EntityJoinOp) planOp).getLeftBranch(), joinCost.getLeftBranchCost()), context));
                traversal.by(planTraversalTranslator.translate(new PlanWithCost<>(((EntityJoinOp) planOp).getRightBranch(), joinCost.getRightBranchCost()), context));
            }
        }
        return traversal;
    }
}
