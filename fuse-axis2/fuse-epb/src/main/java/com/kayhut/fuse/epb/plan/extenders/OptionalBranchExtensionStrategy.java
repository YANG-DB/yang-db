package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.OptionalOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.query.EBase;
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

        PlanOp lastPlanOp = plan.get().getOps().get(plan.get().getOps().size() - 1);
        return OptionalOp.class.isAssignableFrom(lastPlanOp.getClass()) ?
            continueOptionalOp((OptionalOp)lastPlanOp) :
            startNewOptionalOps(plan, query);
    }
    //endregion

    //region Private Methods
    private boolean nearOptionalComponents(EntityOp entityOp) {
        Iterable<AsgEBase<OptionalComp>> optionalElements = Collections.emptyList();
        Optional<AsgEBase<Quant1>> lastEntityQuant = AsgQueryUtil.nextAdjacentDescendant(entityOp.getAsgEbase(), Quant1.class);
        if (lastEntityQuant.isPresent()) {
            optionalElements = AsgQueryUtil.nextAdjacentDescendants(lastEntityQuant.get(), OptionalComp.class);
        } else {
            Optional<AsgEBase<OptionalComp>> optionalElement = AsgQueryUtil.nextAdjacentDescendant(entityOp.getAsgEbase(), OptionalComp.class);
            if (optionalElement.isPresent()) {
                optionalElements = Collections.singletonList(optionalElement.get());
            }
        }

        return !Stream.ofAll(optionalElements).isEmpty();
    }

    private Iterable<Plan> continueOptionalOp(OptionalOp optionalOp) {
        return null;
    }

    private Iterable<Plan> startNewOptionalOps(Optional<Plan> plan, AsgQuery query) {
        Optional<EntityOp> lastEntityOp = PlanUtil.last(plan.get(), EntityOp.class);
        if (!lastEntityOp.isPresent()) {
            return Collections.emptyList();
        }

        if (!nearOptionalComponents(lastEntityOp.get())) {
            return Collections.emptyList();
        }

        for(Plan extendedPlan : super.extendPlan(plan, query)) {
            lastEntityOp = PlanUtil.first(extendedPlan, lastEntityOp.get());
            if (!lastEntityOp.isPresent()) {
                continue;
            }

            Optional<RelationOp> nextRelationOp = PlanUtil.next(extendedPlan, lastEntityOp.get(), RelationOp.class);
            if (!nextRelationOp.isPresent()) {
                continue;
            }

            Optional<AsgEBase<? extends EBase>> optionalComp = Stream.ofAll(AsgQueryUtil.path(query, lastEntityOp.get().getAsgEbase().geteNum(), nextRelationOp.get().getAsgEbase().geteNum()))
                    .filter(asgEBase -> asgEBase.geteBase() instanceof OptionalComp)
                    .toJavaOptional();

            if (optionalComp.isPresent()) {
                extendedPlan.to(nextRelationOp.get())
                        .withOp(new OptionalOp((AsgEBase<OptionalComp>)optionalComp.get(),
                                extendedPlan.from(nextRelationOp.get()).getOps()));
            }
        }

        return super.extendPlan(plan, query);
    }
    //endregion
}
