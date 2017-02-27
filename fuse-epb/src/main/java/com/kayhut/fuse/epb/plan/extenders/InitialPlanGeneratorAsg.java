package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.execution.plan.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.query.EConcrete;
import com.kayhut.fuse.model.query.ETyped;
import com.kayhut.fuse.model.query.EUntyped;
import com.kayhut.fuse.model.queryAsg.AsgQuery;
import com.kayhut.fuse.model.queryAsg.EBaseAsg;
import com.kayhut.fuse.model.results.Entity;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by moti on 2/27/2017.
 */
public class InitialPlanGeneratorAsg implements PlanExtensionStrategy<Plan, AsgQuery> {
    @Override
    public Iterable<Plan> extendPlan(Plan plan, AsgQuery query) {
        return null;
    }

    private void recursiveSeedGenerator(EBaseAsg asgNode,  List<Plan> plans ){
        boolean found = false;
        String tag = null;
        if(asgNode.geteBase() instanceof EConcrete ){
            EConcrete eConcrete = (EConcrete) asgNode.geteBase();
            tag = eConcrete.geteTag();
            found = true;
        }
        else if(asgNode.geteBase() instanceof ETyped ){
            ETyped eTyped = (ETyped) asgNode.geteBase();
            tag = eTyped.geteTag();
            found = true;
        }
        else if(asgNode.geteBase() instanceof EUntyped){
            EUntyped eUntyped = (EUntyped) asgNode.geteBase();
            tag = eUntyped.geteTag();
            found = true;
        }
        if(found) {
            EntityOp op = new EntityOp(tag);
            op.seteNum(asgNode.geteNum());
        }


    }
}
