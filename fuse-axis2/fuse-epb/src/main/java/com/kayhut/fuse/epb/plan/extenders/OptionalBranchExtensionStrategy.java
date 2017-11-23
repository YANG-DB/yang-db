package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import com.kayhut.fuse.model.query.quant.Quant1;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.Optional;

/**
 * Created by roman.margolis on 23/11/2017.
 */
public class OptionalBranchExtensionStrategy extends CompositePlanExtensionStrategy<Plan,AsgQuery> {
    //region Constructors
    public OptionalBranchExtensionStrategy(PlanExtensionStrategy<Plan, AsgQuery>...innerExtender) {
        super(innerExtender);
    }

    public OptionalBranchExtensionStrategy(PlanExtensionStrategy<Plan, AsgQuery> innerExtender) {
        super(innerExtender);
    }
    //endregion

    //region PlanExtensionStrategy Imlementation
    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (!plan.isPresent()) {
            return Collections.emptyList();
        }

        Optional<EntityOp> lastEntityOp = PlanUtil.last(plan.get(), EntityOp.class);
        if (!lastEntityOp.isPresent()) {
            return Collections.emptyList();
        }

        return super.extendPlan(plan, query);
    }
    //endregion
}
