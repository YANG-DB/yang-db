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
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.quant.Quant1;
import javaslang.collection.Stream;

import java.util.*;

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
                isOptionalOpComplete((OptionalOp) lastPlanOp, query) ?
                        startNewOptionalOps(plan.get(), query) :
                        continueOptionalOp(plan.get(), (OptionalOp) lastPlanOp, query) :
                startNewOptionalOps(plan.get(), query);
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

    private Iterable<Plan> continueOptionalOp(Plan plan, OptionalOp optionalOp, AsgQuery query) {
        Plan priorOptionalPlan = plan.withoutOp(optionalOp);
        return Stream.ofAll(super.extendPlan(Optional.of(new Plan(optionalOp.getOps())), query))
                .map(extendedPlan -> priorOptionalPlan.<Plan>withOp(new OptionalOp(optionalOp.getAsgEbase(), extendedPlan.getOps())))
                .toJavaList();
    }

    private Iterable<Plan> startNewOptionalOps(Plan plan, AsgQuery query) {
        Optional<EntityOp> lastEntityOp = PlanUtil.last(plan, EntityOp.class);
        if (!lastEntityOp.isPresent()) {
            return Collections.emptyList();
        }

        if (!nearOptionalComponents(lastEntityOp.get())) {
            return Collections.emptyList();
        }

        //TODO: optimization: retain only the optional query part
        List<Plan> plansWithOptionals = new ArrayList<>();
        for(Plan extendedPlan : super.extendPlan(Optional.of(plan), query)) {
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
                plansWithOptionals.add(extendedPlan.to(nextRelationOp.get())
                        .withOp(new OptionalOp((AsgEBase<OptionalComp>)optionalComp.get(),
                                extendedPlan.from(nextRelationOp.get()).getOps())));
            }
        }

        return plansWithOptionals;
    }

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
                .filter(planOp -> AsgEBaseContainer.class.isAssignableFrom(planOp.getClass()))
                .map(planOp -> ((AsgEBaseContainer)planOp).getAsgEbase().geteBase().geteNum())
                .toJavaSet();

        return optionalEnums.equals(optionalOpEnums);
    }
    //endregion
}
