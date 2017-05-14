package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.model.execution.plan.PlanUtil;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
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
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Optional;

/**
 * Created by Roman on 10/05/2017.
 */
public class EntityOpTranslationStrategy implements PlanOpTranslationStrategy {
    //region PlanOpTranslationStrategy Implementation
    @Override
    public GraphTraversal translate(GraphTraversal traversal, Plan plan, PlanOpBase planOp, TranslationContext context) {
        if (!(planOp instanceof EntityOp)) {
            return traversal;
        }

        EntityOp entityOp = (EntityOp)planOp;

        if (PlanUtil.isFirst(plan, entityOp)) {
            traversal = context.getGraphTraversalSource().V().as(entityOp.getAsgEBase().geteBase().geteTag());

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
            Optional<PlanOpBase> previousPlanOp = PlanUtil.getAdjacentPrev(plan, planOp);
            if (previousPlanOp.isPresent() && previousPlanOp.get() instanceof RelationOp) {
                return traversal.otherV().as(entityOp.getAsgEBase().geteBase().geteTag());
            }
        }

        return traversal;
    }
    //endregion
}
