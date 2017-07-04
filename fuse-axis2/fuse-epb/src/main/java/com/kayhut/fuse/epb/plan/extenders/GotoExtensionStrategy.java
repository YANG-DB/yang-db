package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.kayhut.fuse.epb.plan.extenders.SimpleExtenderUtils.getLastOpOfType;

public class GotoExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {

    @Override
    public Iterable<Plan> extendPlan(Plan plan, AsgQuery query) {
        List<Plan> plans = new ArrayList<>();

        EntityOp lastEntityOp = getLastOpOfType(plan, EntityOp.class);
        List<EntityOp> ops = plan.getOps().stream().filter(op -> ((op instanceof EntityOp) && !(op instanceof GoToEntityOp) && !op.equals(lastEntityOp)))
                .map(op -> (EntityOp) op).collect(Collectors.toList());

        for (EntityOp ancestor : ops) {
            Plan newPlan = plan.withOp(new GoToEntityOp(ancestor.getAsgEBase()));

            if(!Plan.equals(plan, newPlan)) {
                newPlan.log("GotoExtensionStrategy:[" + Plan.diff(plan, newPlan) + "]", Level.INFO);
            }

            plans.add(newPlan);
        }
        return plans;
    }
}
