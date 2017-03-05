package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.queryAsg.AsgQuery;
import com.kayhut.fuse.model.queryAsg.EBaseAsg;

import java.util.Map;

/**
 * Created by moti on 3/1/2017.
 */
public class SiblingOnlyPlanValidator implements PlanValidator<Plan, AsgQuery>{

    @Override
    public boolean isPlanValid(Plan plan, AsgQuery query) {
        Map<Integer, EBaseAsg> eBaseAsgMap = SimpleExtenderUtils.flattenQuery(query);
        PlanOpBase lastOp = null;
        EBaseAsg lastOpElem = null;
        for(PlanOpBase currentOp : plan.getOps()){
            EBaseAsg currentOpElem = eBaseAsgMap.get(currentOp.geteNum());
            if(lastOp != null){
                boolean found = lastOpElem.getNext().stream().anyMatch(n -> n.geteNum() == currentOpElem.geteNum());
                found |= lastOpElem.getB().stream().anyMatch(n -> n.geteNum() == currentOpElem.geteNum());
                if(!found){
                    return false;
                }
            }
            lastOp = currentOp;
            lastOpElem = currentOpElem;
        }

        return true;
    }
}
