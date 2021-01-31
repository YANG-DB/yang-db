package com.yangdb.fuse.epb.plan.extenders;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.yangdb.fuse.dispatcher.utils.PlanUtil;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.execution.plan.AsgEBaseContainer;
import com.yangdb.fuse.model.execution.plan.PlanOp;
import com.yangdb.fuse.model.execution.plan.composite.OptionalOp;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.entity.EntityNoOp;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.optional.OptionalComp;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

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
                Stream.ofAll(AsgQueryUtil.descendantBDescendants(optionalComp, asgEBase -> classSet.contains(asgEBase.geteBase().getClass()), asgEBase -> true))
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
