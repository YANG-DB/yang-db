package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.util.Optional;

/**
 * Created by benishue on 08-Mar-17.
 *
 * EConcrete =   otherV().has('promise', P.eq(Promise.as(<EConcrete.Id>)).as(<EConcrete.ETag>)
 * ETyped = otherV().has('constraint', P.eq(Constraint.by(__.has('label', P.eq(<Ontology(<ETyped.EType>)>)))).as(<ETyped.ETag>)
 * EUntyped = otherV().as(<EUntyped.ETag>)
 */
public class EntityOpPostRelTranslationStrategy implements TranslationStrategy {

    @Override
    public GraphTraversal apply(TranslationStrategyContext context, GraphTraversal traversal) {
        Plan<?> plan = context.getPlan();
        PlanOpBase currentPlanOpBase = context.getPlanOpBase();
        Ontology ontology = context.getOntology();
        PlanUtil planUtil = new PlanUtil();
        Optional<PlanOpBase> prev = planUtil.getPrev(plan.getOps(), currentPlanOpBase);
        if(!prev.isPresent()) {
            return traversal;
        }

        //EntityOp Post RelOp
        if(currentPlanOpBase instanceof EntityOp && prev.get() instanceof RelationOp) {
            EEntityBase eEntityBase = ((EntityOp) currentPlanOpBase).getEntity().geteBase();

            traversal.otherV();

            if (eEntityBase instanceof EConcrete) {
                traversal.has("promise", P.eq(Promise.as(((EConcrete) eEntityBase).geteID())));
            } else if (eEntityBase instanceof ETyped) {
                String eTypeName = OntologyUtil.getEntityTypeNameById(ontology,((ETyped) eEntityBase).geteType());
                traversal.has("constraint", P.eq(Constraint.by(__.has("label", P.eq(eTypeName)))));
            } else if (eEntityBase instanceof EUntyped) {
                ;
            }

            traversal.as(eEntityBase.geteTag());
        }
        return traversal;

    }


}
