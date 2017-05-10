package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 10/05/2017.
 */
public class EntityOpTranslationStrategy implements TranslationStrategy {
    //region Constructors
    public EntityOpTranslationStrategy(Graph graph) {
        this.graph = graph;
    }
    //endregion

    //region TranslationStrategy Implementation
    @Override
    public GraphTraversal translate(GraphTraversal traversal, PlanOpBase planOp, TranslationStrategyContext context) {
        if (!(planOp instanceof EntityOp)) {
            return traversal;
        }

        EntityOp entityOp = (EntityOp)planOp;

        if (PlanUtil.isFirst(context.getPlan(), entityOp)) {
            traversal = graph.traversal().V().as(entityOp.getAsgEBase().geteBase().geteTag());

            EEntityBase entity = entityOp.getAsgEBase().geteBase();
            if (entity instanceof EConcrete) {
                traversal.has(GlobalConstants.HasKeys.PROMISE, P.eq(Promise.as(((EConcrete) entity).geteID())));
            }
            else if (entity instanceof ETyped) {
                String eTypeName = OntologyUtil.getEntityTypeNameById(context.getOntology(),((ETyped) entity).geteType());
                traversal.has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.has(T.label, P.eq(eTypeName))));
            }
            else if (entity instanceof EUntyped) {
                ;
            }

        } else {
            Optional<PlanOpBase> previousPlanOp = PlanUtil.getAdjacentPrev(context.getPlan(), planOp);
            if (previousPlanOp.isPresent() && previousPlanOp.get() instanceof RelationOp) {
                return traversal.otherV().as(entityOp.getAsgEBase().geteBase().geteTag());
            }
        }

        return traversal;
    }
    //endregion

    //region Fields
    private Graph graph;
    //endregion
}
