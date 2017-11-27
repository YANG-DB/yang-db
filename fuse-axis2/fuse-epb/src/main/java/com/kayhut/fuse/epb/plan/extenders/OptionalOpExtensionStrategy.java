package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.OptionalOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityNoOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import javaslang.collection.Stream;

import java.util.*;

/**
 * Created by roman.margolis on 23/11/2017.
 */
public class OptionalOpExtensionStrategy extends CompositePlanExtensionStrategy<Plan,AsgQuery> {
    //region Constructors
    @SafeVarargs
    public OptionalOpExtensionStrategy(PlanExtensionStrategy<Plan, AsgQuery>...innerExtender) {
        super(innerExtender);
    }

    public OptionalOpExtensionStrategy(PlanExtensionStrategy<Plan, AsgQuery> innerExtender) {
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
        if (OptionalOp.class.isAssignableFrom(lastPlanOp.getClass()) && !isOptionalOpComplete((OptionalOp)lastPlanOp, query)) {
            return extendOptionalOp(plan.get(), (OptionalOp)lastPlanOp, query);
        }

        return Collections.emptyList();
    }
    //endregion

    //region Private Methods
    private boolean isOptionalOpComplete(OptionalOp optionalOp, AsgQuery query) {
        AsgEBase<OptionalComp> optionalComp = AsgQueryUtil.element$(query, optionalOp.getAsgEbase().geteNum());

        final Set<Class<? extends EBase>> classSet = Stream.of(ETyped.class, EConcrete.class, EUntyped.class, Rel.class,
                EProp.class, EPropGroup.class, RelProp.class, RelPropGroup.class)
                .toJavaSet();

        Set<Integer> optionalEnums =
                Stream.ofAll(AsgQueryUtil.nextDescendants(optionalComp, asgEBase -> classSet.contains(asgEBase.geteBase().getClass()), asgEBase -> true))
                        .map(asgEbase -> asgEbase.geteBase().geteNum())
                        .toJavaSet();

        Set<Integer> optionalOpEnums = Stream.ofAll(PlanUtil.flat(optionalOp).getOps())
                .filter(planOp -> !EntityNoOp.class.isAssignableFrom(planOp.getClass()))
                .filter(planOp -> AsgEBaseContainer.class.isAssignableFrom(planOp.getClass()))
                .map(planOp -> ((AsgEBaseContainer)planOp).getAsgEbase().geteBase().geteNum())
                .toJavaSet();

        return optionalEnums.equals(optionalOpEnums);
    }

    private Iterable<Plan> extendOptionalOp(Plan plan, OptionalOp optionalOp, AsgQuery query) {
        Plan priorOptionalPlan = plan.withoutOp(optionalOp);
        return Stream.ofAll(super.extendPlan(Optional.of(new Plan(optionalOp.getOps())), query))
                .map(extendedPlan -> priorOptionalPlan.<Plan>withOp(new OptionalOp(optionalOp.getAsgEbase(), extendedPlan.getOps())))
                .toJavaList();
    }
    //endregion
}
