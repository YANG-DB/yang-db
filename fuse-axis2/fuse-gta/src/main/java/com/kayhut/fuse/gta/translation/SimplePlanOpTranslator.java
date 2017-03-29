package com.kayhut.fuse.gta.translation;

import com.kayhut.fuse.gta.strategy.*;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.promise.PromiseGraph;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by moti on 3/7/2017.
 */
public class SimplePlanOpTranslator {
    //region Fields
    private Map<Class, List<TranslationStrategy>> map;
    //endregion

    //region Constructors
    public SimplePlanOpTranslator(PromiseGraph promiseGraph) {
        this.map = new HashMap<>();

        //entity operations types list
        List<TranslationStrategy> strategies = new ArrayList<>();
        strategies.add(new EntityOpStartTranslationStrategy(promiseGraph));
        strategies.add(new EntityOpAdjcentTranslationStrategy());
        strategies.add(new EntityOpPostRelTranslationStrategy());

        this.map.put(EntityOp.class, strategies);

        //relations operations map
        this.map.put(RelationOp.class, Arrays.asList(new RelationOpTranslationStrategy()));
    }
    //endregion

    public <C> GraphTraversal translate(Plan<C> plan, GraphTraversal graphTraversal, Ontology ontology) {
        AtomicReference<GraphTraversal> traversalReference = new AtomicReference<>(graphTraversal);
        // Create initial traversal
        Stream.ofAll(plan.getOps()).forEach(op ->
                map.get(op.getOpBase().getClass()).forEach(strategy -> {
                    traversalReference.set(strategy.apply(new TranslationStrategyContext( op.getOpBase(), plan, ontology), traversalReference.get()));
                }));

    // iterate ops
    // translate each op via factory

        return traversalReference.get();
    }
}
