package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.execution.plan.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.List;
import java.util.Optional;

/**
 * Created by Roman on 09/05/2017.
 */
public class EntityFilterOpTranslationStrategy implements TranslationStrategy {
    //region TranslationStrategy Implementation
    @Override
    public GraphTraversal apply(TranslationStrategyContext context, GraphTraversal traversal) {
        if (!(context.getPlanOp() instanceof EntityFilterOp)) {
            return traversal;
        }

        EntityFilterOp entityFilterOp = (EntityFilterOp)context.getPlanOp();
        Optional<PlanOpBase> previousPlanOp = PlanUtil.getPrev(context.getPlan(), entityFilterOp);
        if (!previousPlanOp.isPresent()) {
            return traversal;
        }

        EntityOp entityOp = (EntityOp)previousPlanOp.get();
        if (PlanUtil.isFirst(context.getPlan(), entityOp)) {
            traversal = appendFirstEntityFilterOp(traversal, entityFilterOp, context.getOntology());
        } else if (!entityFilterOp.getAsgEBase().geteBase().geteProps().isEmpty()) {
            traversal = appendEntityFilterOp(traversal, entityFilterOp, context.getOntology());
        }

        return traversal;
    }
    //endregion

    //region Private Methods
    private GraphTraversal appendFirstEntityFilterOp(
            GraphTraversal traversal,
            EntityFilterOp entityFilterOp,
            Ontology ontology) {

        EEntityBase eEntityBase = entityFilterOp.getEntity().geteBase();

        if (eEntityBase instanceof EConcrete) {
            traversal.has(GlobalConstants.HasKeys.PROMISE, P.eq(Promise.as(((EConcrete) eEntityBase).geteID())));
        }
        else if (eEntityBase instanceof ETyped) {
            String eTypeName = OntologyUtil.getEntityTypeNameById(ontology,((ETyped) eEntityBase).geteType());
            Traversal constraintTraversal = __.has(T.label, P.eq(eTypeName));
            List<Traversal> epropTraversals =
                    Stream.ofAll(entityFilterOp.getAsgEBase().geteBase().geteProps())
                        .map(eProp -> convertEPropToTraversal(eProp, ontology)).toJavaList();

            if (!epropTraversals.isEmpty()) {
                epropTraversals.add(0, constraintTraversal);
                constraintTraversal = __.and(Stream.ofAll(epropTraversals).toJavaArray(Traversal.class));
            }

            traversal.has(GlobalConstants.HasKeys.CONSTRAINT, P.eq(constraintTraversal));
        }
        else if (eEntityBase instanceof EUntyped) {
            ;
        }

        return traversal;
    }

    private GraphTraversal appendEntityFilterOp(
            GraphTraversal traversal,
            EntityFilterOp entityFilterOp,
            Ontology ontology) {

        List<Traversal> epropTraversals =
                Stream.ofAll(entityFilterOp.getAsgEBase().geteBase().geteProps())
                        .map(eProp -> convertEPropToTraversal(eProp, ontology)).toJavaList();

        Traversal constraintTraversal = epropTraversals.size() == 1 ?
                epropTraversals.get(0) :
                __.and(Stream.ofAll(epropTraversals).toJavaArray(Traversal.class));

        traversal.outE(GlobalConstants.Labels.PROMISE_FILTER)
                .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(constraintTraversal));

        return traversal;
    }

    private Traversal convertEPropToTraversal(EProp eProp, Ontology ontology) {
         Optional<Property> property = OntologyUtil.getProperty(ontology, Integer.parseInt(eProp.getpType()));
         if (!property.isPresent()) {
             return __.start();
         }

         return __.has(property.get().getName(), convertConstraintToGremlinPredicate(eProp.getCon()));
    }

    private P convertConstraintToGremlinPredicate(com.kayhut.fuse.model.query.Constraint constraint){
        switch (constraint.getOp()) {
            case eq: return P.eq(constraint.getExpr());
            case ne: return P.neq(constraint.getExpr());
            case gt: return P.gt(constraint.getExpr());
            case lt: return P.lt(constraint.getExpr());
            case ge: return P.gte(constraint.getExpr());
            case le: return P.lte(constraint.getExpr());
            case inRange:
                Object[] range = (Object[])constraint.getExpr();
                return P.between(range[0], range[1]);
            case inSet: return P.within(constraint.getExpr());
            case notInSet: return P.without(constraint.getExpr());
            default: throw new RuntimeException("not supported");
        }
    }
    //endregion
}
