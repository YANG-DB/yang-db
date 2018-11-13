package com.kayhut.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by roman.margolis on 15/03/2018.
 */
public class KnowledgeInitialPlanGeneratorExtensionStrategy implements PlanExtensionStrategy<Plan, AsgQuery> {
    public KnowledgeInitialPlanGeneratorExtensionStrategy() {
        this.strongPTypes = Stream.of("id", "logicalId", "relationId", "entityIds").toJavaSet();
        this.strongOps = Stream.of(ConstraintOp.eq, ConstraintOp.inSet).toJavaSet();
    }

    //region PlanExtensionStrategy Implementation
    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        if (plan.isPresent()) {
            return Collections.emptyList();
        }

        List<AsgEBase<EEntityBase>> entitySeeds = AsgQueryUtil.nextDescendants(
                query.getStart(),
                asgEBase -> EEntityBase.class.isAssignableFrom(asgEBase.geteBase().getClass()),
                asgEBase -> !OptionalComp.class.isAssignableFrom(asgEBase.geteBase().getClass()));

        List<Plan> seeds =
                Stream.ofAll(entitySeeds)
                .filter(seed -> ETyped.class.isAssignableFrom(seed.geteBase().getClass()))
                .filter(seed -> !((ETyped)seed.geteBase()).geteType().equals("LogicalEntity"))
                .map(entitySeed -> {
                    Optional<AsgEBase<Quant1>> entitySeedQuant = AsgQueryUtil.nextAdjacentDescendant(entitySeed, Quant1.class);
                    Optional<AsgEBase<EPropGroup>> epropGroup;
                    if (entitySeedQuant.isPresent()) {
                        epropGroup = AsgQueryUtil.nextAdjacentDescendant(entitySeedQuant.get(), EPropGroup.class);
                    } else {
                        epropGroup = AsgQueryUtil.nextAdjacentDescendant(entitySeed, EPropGroup.class);
                    }

                    Plan newPlan = new Plan(new EntityOp(entitySeed));
                    if (epropGroup.isPresent()) {
                        newPlan = newPlan.withOp(new EntityFilterOp(epropGroup.get()));
                    }

                    return newPlan;
                }).toJavaList();

        List<Plan> strongSeeds =
                Stream.ofAll(seeds)
                .filter(newPlan -> {
                    EntityOp entityOp = PlanUtil.first(newPlan, EntityOp.class).get();
                    if (EConcrete.class.isAssignableFrom(entityOp.getAsgEbase().geteBase().getClass())) {
                        return true;
                    }

                    EntityFilterOp entityFilterOp = PlanUtil.first(newPlan, EntityFilterOp.class).get();
                    return !Stream.ofAll(entityFilterOp.getAsgEbase().geteBase().getProps())
                            .filter(eProp -> eProp.getCon() != null)
                            .filter(eProp -> this.strongPTypes.contains(eProp.getpType()))
                            .filter(eProp -> this.strongOps.contains(eProp.getCon().getOp()))
                            .isEmpty();
                }).toJavaList();

        if (strongSeeds.size() > 0) {
            return Collections.singletonList(strongSeeds.get(0));
        }

        return seeds;
    }
    //endregion

    //region Fields
    private Set<String> strongPTypes;
    private Set<ConstraintOp> strongOps;
    //endregion
}
