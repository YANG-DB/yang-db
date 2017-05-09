package com.kayhut.fuse.gta.strategy;

import com.google.inject.Inject;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
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
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;

/**
 * Created by benishue on 08-Mar-17.
 *
 *
 *
 * EConcrete =   g.V().has('promise', P.eq(Promise.as(<EConcrete.Id>)).as(<EConcrete.ETag>)
 * ETyped = g.V().has('constraint', P.eq(Constraint.by(__.has('label', P.eq(<Ontology(<ETyped.EType>)>)))).as(<ETyped.ETag>)
 * EUntyped = g.V().as(<EUntyped.ETag>)
 */
public class EntityOpStartTranslationStrategy implements TranslationStrategy {
    private Graph graph;

    @Inject
    public EntityOpStartTranslationStrategy(Graph graph) {
        this.graph = graph;
    }

    @Override
    public GraphTraversal apply(TranslationStrategyContext context, GraphTraversal traversal) {
        Plan plan = context.getPlan();
        PlanOpBase planOpBase = context.getPlanOp();
        Ontology ontology = context.getOntology();
        if(PlanUtil.isFirst(plan, planOpBase)) {
            EEntityBase eEntityBase = ((EntityOp) planOpBase).getAsgEBase().geteBase();
            String entityETag = ((EntityOp)planOpBase).getAsgEBase().geteBase().geteTag();

            //Creating the Graph
            traversal = graph.traversal().V();

            if (eEntityBase instanceof EConcrete) {
                traversal.has("promise", P.eq(Promise.as(((EConcrete) eEntityBase).geteID())));
            }
            else if (eEntityBase instanceof ETyped) {
                String eTypeName = OntologyUtil.getEntityTypeNameById(ontology,((ETyped) eEntityBase).geteType());
                traversal.has("constraint", P.eq(Constraint.by(__.has(T.label, P.eq(eTypeName)))));
            }
            else if (eEntityBase instanceof EUntyped) {
                ;
            }

            traversal.as(entityETag);
        }
        return traversal;

    }
}
