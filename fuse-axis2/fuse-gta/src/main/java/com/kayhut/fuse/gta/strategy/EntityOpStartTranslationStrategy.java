package com.kayhut.fuse.gta.strategy;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyUtil;
import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.Constraint;
import com.kayhut.fuse.model.query.entity.*;
import com.kayhut.fuse.unipop.Promise;
import javaslang.Tuple2;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;

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
        PlanOpBase planOpBase = context.getPlanOpBase();
        Ontology ontology = context.getOntology();
        PlanUtil planUtil = new PlanUtil();
        if(planUtil.isFirst(plan.getOps(),planOpBase)) {
            EEntityBase eEntityBase = ((EntityOp) planOpBase).getEntity().geteBase();
            String entityETag = ((EntityOp)planOpBase).getEntity().geteBase().geteTag();

            //Creating the Graph
            traversal = new GraphTraversalSource(graph).V();

            if (eEntityBase instanceof EConcrete) {
                traversal.has("promise", P.eq(Promise.as(((EConcrete) eEntityBase).geteID())));
            }
            else if (eEntityBase instanceof ETyped) {
                String eTypeName = OntologyUtil.getEntityTypeNameById(ontology,((ETyped) eEntityBase).geteType());
                traversal.has("constraint", P.eq(Constraint.by(__.has("label", P.eq(eTypeName)))));
            }
            else if (eEntityBase instanceof EUntyped) {
                ;
            }

            traversal.as(entityETag);
        }
        return traversal;

    }
}
