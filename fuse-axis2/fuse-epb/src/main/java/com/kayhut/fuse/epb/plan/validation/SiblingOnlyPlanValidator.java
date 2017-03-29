package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.PlanOpWithCost;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.Rel;

import java.util.Map;

/**
 * Created by moti on 3/1/2017.
 */
public class SiblingOnlyPlanValidator<C> implements PlanValidator<Plan<C>, AsgQuery>{

    @Override
    public boolean isPlanValid(Plan<C> plan, AsgQuery query) {
        Map<Integer, AsgEBase> eBaseAsgMap = SimpleExtenderUtils.flattenQuery(query);
        PlanOpWithCost<C> previousOp = null;
        AsgEBase previousOpElem = null;
        for(PlanOpWithCost<C> currentOp : plan.getOps()){
            AsgEBase currentOpElem = eBaseAsgMap.get(currentOp.getOpBase().geteNum());
            if(previousOp != null){
                if (!assertElementOrderIsValid(previousOpElem, currentOp, currentOpElem)) return false;
            }
            previousOp = currentOp;
            previousOpElem = currentOpElem;
        }
        return true;
    }

    private boolean assertElementOrderIsValid(AsgEBase previousOpElem, PlanOpWithCost<C> currentOp, AsgEBase currentOpElem) {
        boolean found = previousOpElem.getNext().stream().anyMatch(n -> ((AsgEBase)n).geteNum() == currentOpElem.geteNum());
        found |= previousOpElem.getB().stream().anyMatch(n -> ((AsgEBase)n).geteNum() == currentOpElem.geteNum());
        if(!found){
            found |= previousOpElem.getParents().stream().anyMatch(n -> ((AsgEBase)n).geteNum() == currentOpElem.geteNum());
            // If we are here through parent, check the direction is opposite to the original one
            if(found){
                if(currentOp.getOpBase() instanceof RelationOp) {
                    RelationOp relationOp = (RelationOp) currentOp.getOpBase();
                    if (!isOpposite(relationOp.getRelation().geteBase().getDir(), ((AsgEBase<Rel>) currentOpElem).geteBase().getDir())) {
                        return false;
                    }
                }
            }else {
                return false;
            }
        }else{
            // Assert direction is the same as the original
            if(currentOp.getOpBase() instanceof RelationOp) {
                RelationOp relationOp = (RelationOp) currentOp.getOpBase();
                if (!relationOp.getRelation().geteBase().getDir().equals(((AsgEBase<Rel>) currentOpElem).geteBase().getDir())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isOpposite(String dir1, String dir2) {
        return (dir1.equals("R") && dir2.equals("L") ) ||(dir1.equals("L") && dir2.equals("R")) || dir1.equals("-");
    }
}
