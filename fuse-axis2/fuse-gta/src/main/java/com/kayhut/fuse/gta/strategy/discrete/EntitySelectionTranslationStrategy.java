package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.strategy.utils.TraversalUtil;
import com.kayhut.fuse.gta.translation.TranslationContext;
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
    public EntitySelectionTranslationStrategy(Class<? extends PlanOp> klasses) {
        super(klasses);
    }

    public EntitySelectionTranslationStrategy(Predicate<PlanOp> planOpPredicate) {
        super(planOpPredicate);
    }
    //endregion

    //region PlanOpTranslationStrategyBase Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOp planOp, TranslationContext context) {
        Optional<EntityOp> lastEntityOp = EntityOp.class.equals(planOp.getClass()) ?
                Optional.of((EntityOp)planOp) :
                PlanUtil.prev(plan.getPlan(), planOp, EntityOp.class);

        if (!lastEntityOp.isPresent()) {
            return traversal;
        }

        if (lastEntityOp.get().getAsgEbase().geteBase().getReportProps().isEmpty()) {
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

        Stream.ofAll(lastEntityOp.get().getAsgEbase().geteBase().getReportProps())
                .forEach(eProp -> traversal.has(context.getOnt().$property$(eProp).getName(),
                        SelectP.raw(context.getOnt().$property$(eProp).getName())));

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
