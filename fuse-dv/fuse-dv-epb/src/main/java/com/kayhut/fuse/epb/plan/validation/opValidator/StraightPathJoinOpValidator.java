package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.dispatcher.epb.PlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.validation.ChainedPlanValidator;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.CompositePlanOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.validation.ValidationResult;

import java.util.*;

public class StraightPathJoinOpValidator implements ChainedPlanValidator.PlanOpValidator {
    private boolean isPathAvailable(EntityJoinOp entityJoinOp, AsgQuery query) {
        Set<Integer> leftEntities = getEntityOpsRecursively(entityJoinOp.getLeftBranch().getOps(),new HashSet<>());
        EntityOp lastRightEntity = PlanUtil.last(entityJoinOp.getRightBranch(), EntityOp.class).get();
        Set<Integer> visitedNodes = new HashSet<>();
        Queue<Integer> seeds = new LinkedList<>();
        seeds.add(lastRightEntity.getAsgEbase().geteNum());
        while(!seeds.isEmpty()){
            Integer currentEntity = seeds.poll();
            if(currentEntity == entityJoinOp.getAsgEbase().geteNum())
                return true;
            visitedNodes.add(currentEntity);


            AsgEBase<EBase> currentElement = AsgQueryUtil.element$(query, currentEntity);


            List<AsgEBase<EBase>> descendants = AsgQueryUtil.nextDescendantsSingleHop(currentElement, EEntityBase.class);
            for (AsgEBase<EBase> descendant : descendants) {
                if(entityJoinOp.getAsgEbase().geteNum() == descendant.geteNum())
                    return true;
                if(!leftEntities.contains(descendant.geteNum()) && !visitedNodes.contains(descendant.geteNum()))
                    seeds.add(descendant.geteNum());
            }

            Optional<AsgEBase<EBase>> ancestor = AsgQueryUtil.ancestor(currentElement, EEntityBase.class);
            if(ancestor.isPresent()) {
                if (entityJoinOp.getAsgEbase().geteNum() == ancestor.get().geteNum())
                    return true;
                if (!leftEntities.contains(ancestor.get().geteNum()) && !visitedNodes.contains(ancestor.get().geteNum()))
                    seeds.add(ancestor.get().geteNum());
            }

        }

        return false;
    }

    private Set<Integer> getEntityOpsRecursively(List<PlanOp> ops, Set<Integer> set) {
        for (PlanOp op : ops) {
            if (op instanceof EntityOp) {
                set.add(((EntityOp) op).getAsgEbase().geteNum());
            }
            if (op instanceof EntityJoinOp) {
                getEntityOpsRecursively(((EntityJoinOp) op).getLeftBranch().getOps(), set);
                getEntityOpsRecursively(((EntityJoinOp) op).getRightBranch().getOps(), set);
            }
        }
        return set;
    }

    @Override
    public void reset() {

    }

    @Override
    public ValidationResult isPlanOpValid(AsgQuery query, CompositePlanOp compositePlanOp, int opIndex) {
        PlanOp planOp = compositePlanOp.getOps().get(opIndex);
        if(planOp instanceof EntityJoinOp){
            EntityJoinOp join = (EntityJoinOp) planOp;
            if(opIndex == 0 && compositePlanOp.getOps().size() == 1 && !isPathAvailable(join, query)){
                return new ValidationResult(false, "JoinOp path validation failed: " + IterablePlanOpDescriptor.getSimple().describe(compositePlanOp.getOps()));
            }
        }
        return ValidationResult.OK;
    }
}
