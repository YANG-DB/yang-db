package com.kayhut.fuse.gta.strategy.discrete;

/*-
 * #%L
 * fuse-dv-gta
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

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.strategy.utils.TraversalUtil;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.predicates.SelectP;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.EdgeOtherVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by Roman on 28/05/2017.
 */
public class EntitySelectionTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public EntitySelectionTranslationStrategy() {
        super(planOp -> planOp.getClass().equals(EntityFilterOp.class));
    }
    //endregion

    //region PlanOpTranslationStrategyBase Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        EntityFilterOp lastEntityFilterOp = (EntityFilterOp)planOp;
        Optional<EntityOp> lastEntityOp = PlanUtil.prev(plan.getPlan(), lastEntityFilterOp, EntityOp.class);

        if (!lastEntityOp.isPresent()) {
            return traversal;
        }

        if (Stream.ofAll(lastEntityFilterOp.getAsgEbase().geteBase().getProps())
                .filter(eProp -> eProp.getProj() != null).isEmpty()) {
            return traversal;
        }

        if (!PlanUtil.isFirst(plan.getPlan(), lastEntityOp.get())) {
            Optional<VertexStep> lastVertexStep = TraversalUtil.last(traversal, VertexStep.class);
            if (!lastVertexStep.isPresent()) {
                return traversal;
            }

            if (!isFilterVertexStep(lastVertexStep.get())) {
                traversal.outE(GlobalConstants.Labels.PROMISE_FILTER);
            } else {
                Optional<EdgeOtherVertexStep> lastEdgeOtherVertexStep =
                        TraversalUtil.next(traversal, lastVertexStep.get(), EdgeOtherVertexStep.class);
                lastEdgeOtherVertexStep.ifPresent(edgeOtherVertexStep ->
                        TraversalUtil.remove(traversal, edgeOtherVertexStep));
            }
        }

        Stream.ofAll(TraversalUtil.lastConsecutiveSteps(traversal, HasStep.class))
                .filter(hasStep -> isSelectionHasStep((HasStep<?>)hasStep))
                .forEach(step -> traversal.asAdmin().removeStep(step));

        Stream.ofAll(lastEntityFilterOp.getAsgEbase().geteBase().getProps())
                .filter(eProp -> eProp.getProj() != null)
                .forEach(eProp -> traversal.has(context.getOnt().$property$(eProp.getpType()).getName(),
                        SelectP.raw(context.getOnt().$property$(eProp.getpType()).getName())));

        if (PlanUtil.isFirst(plan.getPlan(), lastEntityOp.get())) {
            return traversal;
        }

        Stream.ofAll(TraversalUtil.<Step>lastSteps(traversal, step -> step.getLabels().contains(lastEntityOp.get().getAsgEbase().geteBase().geteTag())))
                .forEach(step -> step.removeLabel(lastEntityOp.get().getAsgEbase().geteBase().geteTag()));

        return traversal.otherV().as(lastEntityOp.get().getAsgEbase().geteBase().geteTag());
    }
    //endregion

    //region Private Methods
    private boolean isFilterVertexStep(VertexStep vertexStep) {
        return !Stream.of(vertexStep.getEdgeLabels())
                .filter(edgeLabel -> edgeLabel.equals(GlobalConstants.Labels.PROMISE_FILTER))
                .isEmpty();

    }

    private boolean isSelectionHasStep(HasStep<?> hasStep) {
        return !Stream.ofAll(hasStep.getHasContainers())
                .filter(hasContainer -> hasContainer.getBiPredicate() instanceof SelectP)
                .isEmpty();
    }
    //endregion
}
