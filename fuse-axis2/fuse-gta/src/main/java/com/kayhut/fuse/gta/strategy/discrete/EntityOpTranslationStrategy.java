package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.gta.strategy.PlanOpTranslationStrategyBase;
import com.kayhut.fuse.gta.strategy.common.EntityTranslationOptions;
import com.kayhut.fuse.gta.strategy.utils.EntityTranslationUtil;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 10/05/2017.
 */
public class EntityOpTranslationStrategy extends PlanOpTranslationStrategyBase {
    //region Constructors
    public EntityOpTranslationStrategy(EntityTranslationOptions options) {
        super(EntityOp.class);
        this.options = options;
    }
    //endregion

    //region PlanOpTranslationStrategy Implementation
    @Override
    protected GraphTraversal translateImpl(GraphTraversal traversal, PlanWithCost<Plan, PlanDetailedCost> plan, PlanOpBase planOp, TranslationContext context) {
        EntityOp entityOp = (EntityOp)planOp;

        if (PlanUtil.isFirst(plan.getPlan(), planOp)) {
            traversal = context.getGraphTraversalSource().V().as(entityOp.getAsgEBase().geteBase().geteTag());
            appendEntity(traversal, entityOp.getAsgEBase().geteBase(), context.getOnt());
        } else {
            Optional<PlanOpBase> previousPlanOp = PlanUtil.adjacentPrev(plan.getPlan(), planOp);
            if (previousPlanOp.isPresent() &&
                    (previousPlanOp.get() instanceof RelationOp ||
                     previousPlanOp.get() instanceof RelationFilterOp)) {
                RelationOp relationOp = previousPlanOp.get() instanceof RelationOp ?
                        (RelationOp)previousPlanOp.get() :
                        PlanUtil.prev(plan.getPlan(), previousPlanOp.get(), RelationOp.class).get();

                switch (this.options) {
                    case none:
                        switch (relationOp.getAsgEBase().geteBase().getDir()) {
                            case R: return traversal.inV().as(entityOp.getAsgEBase().geteBase().geteTag());
                            case L: return traversal.outV().as(entityOp.getAsgEBase().geteBase().geteTag());
                            case RL: return traversal.otherV().as(entityOp.getAsgEBase().geteBase().geteTag());
                        }
                    case filterEntity:
                        //TODO: add vertex filtering mechanism for discrete queries
                }
            }
        }

        return traversal;
    }
    //endregion

    //region Private Methods
    private GraphTraversal<?, ?> appendEntity(GraphTraversal<?, ?> traversal,
                                        EEntityBase entity,
                                        Ontology.Accessor ont) {

        if (entity instanceof EConcrete) {
            traversal.has(T.id, P.eq(((EConcrete)entity).geteID()));
        }
        else if (entity instanceof ETyped || entity instanceof EUntyped) {
            List<String> eTypeNames = EntityTranslationUtil.getValidEntityNames(ont, entity);
            if (eTypeNames.size() == 1) {
                traversal.has(T.label, P.eq(eTypeNames.get(0)));
            } else if (eTypeNames.size() > 1) {
                traversal.has(T.label, P.within(eTypeNames));
            }
        }

        return traversal;
    }
    //endregion

    //region Fields
    private EntityTranslationOptions options;
    //endregion
}
