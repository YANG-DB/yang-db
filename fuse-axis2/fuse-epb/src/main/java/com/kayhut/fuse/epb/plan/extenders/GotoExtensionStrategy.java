package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.entity.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import javaslang.collection.Stream;

import java.util.*;

public class GotoExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent()) {
            return Collections.emptyList();
        }

        List<Plan> plans = new ArrayList<>();

        EntityOp lastEntityOp = PlanUtil.last$(plan.get(), EntityOp.class);

        List<EntityOp> entityOps = getEntityOps(plan.get(), lastEntityOp);

        plan.get().getOps().stream().filter(op -> op instanceof EntityJoinOp).forEach(op -> entityOps.addAll(extractEntityOpFromJoin((EntityJoinOp) op)));
        Map<Integer, AsgEBase<EEntityBase>> ancestors = new HashMap<>();

        for (EntityOp entityOp : entityOps) {
            if(entityOp.getAsgEbase() != lastEntityOp.getAsgEbase())
                ancestors.put(entityOp.getAsgEbase().geteNum(), entityOp.getAsgEbase());
        }

        for (AsgEBase<EEntityBase> ancestor : ancestors.values()) {
            Plan newPlan = plan.get().withOp(new GoToEntityOp(ancestor));

            plans.add(newPlan);
        }
        return plans;
    }

    private List<EntityOp> getEntityOps(Plan plan, EntityOp lastEntityOp) {
        return Stream.ofAll(plan.getOps())
                .filter(op -> ((op instanceof EntityOp) && !(op instanceof GoToEntityOp) && !op.equals(lastEntityOp)))
                .map(op -> (EntityOp)op)
                .toJavaList();
    }

    private List<EntityOp> extractEntityOpFromJoin(EntityJoinOp joinOp){
        List<EntityOp> ops = getEntityOps(joinOp.getLeftBranch(), null);
        ops.addAll(getEntityOps(joinOp.getRightBranch(), null));
        joinOp.getLeftBranch().getOps().stream().filter(op -> op instanceof EntityJoinOp).forEach(op -> ops.addAll(extractEntityOpFromJoin((EntityJoinOp) op)));
        joinOp.getRightBranch().getOps().stream().filter(op -> op instanceof EntityJoinOp).forEach(op -> ops.addAll(extractEntityOpFromJoin((EntityJoinOp) op)));
        return ops;
    }
}
