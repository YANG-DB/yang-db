package com.kayhut.fuse.gta.strategy;

import com.google.inject.Inject;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import javaslang.Tuple2;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
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
    public GraphTraversal apply(Tuple2<Plan, PlanOpBase> context, GraphTraversal traversal) {
        Plan plan = context._1;
        PlanOpBase planOpBase = context._2;
        if(plan.isFirst(planOpBase)) {
            //Creating the Graph

            traversal = new GraphTraversalSource(graph).V().as(((EntityOp)planOpBase).getEntity().geteBase().geteTag());
            return traversal;
        }
        return traversal;

    }
}
