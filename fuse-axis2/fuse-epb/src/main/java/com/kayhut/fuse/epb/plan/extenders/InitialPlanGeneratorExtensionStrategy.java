package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgEBase;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by moti on 2/27/2017.
 */
public class InitialPlanGeneratorExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    @Override
    public Iterable<Plan> extendPlan(Plan plan, AsgQuery query) {
        List<Plan> plans = new LinkedList<>();
        if(plan == null)
            recursiveSeedGenerator(query.getStart(), plans, new HashSet<>());
        return plans;
    }

    private void recursiveSeedGenerator(AsgEBase<? extends EBase> asgNode, List<Plan> plans, HashSet<AsgEBase> visitedNodes){
        visitedNodes.add(asgNode);
        if(asgNode.geteBase() instanceof EEntityBase){
            EEntityBase eEntityBase = (EEntityBase) asgNode.geteBase();
            EntityOp op = new EntityOp(eEntityBase);
            op.seteNum(asgNode.geteNum());
            op.setEntity((EEntityBase) asgNode.geteBase());
            List<PlanOpBase> ops = new LinkedList<>();
            ops.add(op);
            plans.add(new Plan(ops));
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
