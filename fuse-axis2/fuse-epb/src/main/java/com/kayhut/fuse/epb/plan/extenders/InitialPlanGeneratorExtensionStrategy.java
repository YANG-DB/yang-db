package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.cost.PlanOpCostEstimator;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanOpWithCost;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by moti on 2/27/2017.
 */
public class InitialPlanGeneratorExtensionStrategy<C> implements PlanExtensionStrategy<Plan<C>, AsgQuery> {
    private PlanOpCostEstimator<C> costEstimator;

    public InitialPlanGeneratorExtensionStrategy(PlanOpCostEstimator<C> costEstimator) {
        this.costEstimator = costEstimator;
    }

    @Override
    public Iterable<Plan<C>> extendPlan(Optional<Plan<C>> plan, AsgQuery query) {
        List<Plan<C>> plans = new LinkedList<>();
        if(!plan.isPresent())
            recursiveSeedGenerator(query.getStart(), plans, new HashSet<>());
        return plans;
    }

    private void recursiveSeedGenerator(AsgEBase<? extends EBase> asgNode, List<Plan<C>> plans, HashSet<AsgEBase> visitedNodes){
        visitedNodes.add(asgNode);
        if(asgNode.geteBase() instanceof EEntityBase){
            EntityOp op = new EntityOp((AsgEBase<EEntityBase>) asgNode);
            List<PlanOpWithCost<C>> ops = new LinkedList<>();
            ops.add(new PlanOpWithCost<C>(op, costEstimator.estimateCost(Optional.empty(), op)));
            plans.add(new Plan<C>(ops));
        }
        if(asgNode.getNext() != null) {
            for (AsgEBase<? extends EBase> next : asgNode.getNext()) {
                if (!visitedNodes.contains(next)) {
                    recursiveSeedGenerator(next, plans, visitedNodes);
                }
            }
        }
        if(asgNode.getB() != null) {
            for (AsgEBase<? extends EBase> next : asgNode.getB()) {
                if (!visitedNodes.contains(next)) {
                    recursiveSeedGenerator(next, plans, visitedNodes);
                }
            }
        }
    }
}
