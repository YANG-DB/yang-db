package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.utils.TraversalUtil;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.predicates.SelectP;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.EdgeOtherVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.EmptyStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 28/05/2017.
 */
public class SelectionTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public SelectionTranslationStrategy(Class<? extends PlanOpBase> klasses) {
        super(klasses);
    }
    //endregion

    //region PlanOpTranslationStrategyBase Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, Plan plan, PlanOpBase planOp, TranslationContext context) {
        Optional<EntityOp> lastEntityOp = EntityOp.class.equals(planOp.getClass()) ?
                Optional.of((EntityOp)planOp) :
                PlanUtil.prev(plan, planOp, EntityOp.class);

        if (!lastEntityOp.isPresent()) {
            return traversal;
        }

        if (lastEntityOp.get().getAsgEBase().geteBase().getReportProps().isEmpty()) {
            return traversal;
        }

        if (!PlanUtil.isFirst(plan, lastEntityOp.get())) {
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
                .forEach(hasStep -> traversal.asAdmin().removeStep(hasStep));

        Stream.ofAll(lastEntityOp.get().getAsgEBase().geteBase().getReportProps())
                .forEach(eProp -> traversal.has(context.getOnt().$property$(Integer.parseInt(eProp)).getName(),
                        SelectP.raw(context.getOnt().$property$(Integer.parseInt(eProp)).getName())));

        if (PlanUtil.isFirst(plan, lastEntityOp.get())) {
            return traversal;
        }

        return traversal.otherV();
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
