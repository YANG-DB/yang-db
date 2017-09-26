package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.strategy.utils.TraversalUtil;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.predicates.SelectP;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.EdgeOtherVertexStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.VertexStep;

import java.util.Optional;

/**
 * Created by Roman on 28/05/2017.
 */
public class EntitySelectionTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public EntitySelectionTranslationStrategy(Class<? extends PlanOpBase> klasses) {
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

        Stream.ofAll(lastEntityOp.get().getAsgEBase().geteBase().getReportProps())
                .forEach(eProp -> traversal.has(context.getOnt().$property$(eProp).getName(),
                        SelectP.raw(context.getOnt().$property$(eProp).getName())));

        return traversal;
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
