package com.kayhut.fuse.gta.translation;

import com.kayhut.fuse.gta.strategy.*;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.ontology.Ontology;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.unipop.structure.UniGraph;

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
    public SimplePlanOpTranslator(UniGraph promiseGraph) {
        this.map = new HashMap<>();

        this.map.put(EntityOp.class, Arrays.asList(
                new EntityOpStartTranslationStrategy(promiseGraph),
                new EntityOpPostRelTranslationStrategy()
        ));

        this.map.put(GoToEntityOp.class, Collections.singletonList(
                new GoToEntityOpTranslationStrategy()
        ));

        this.map.put(RelationOp.class, Collections.singletonList(
                new RelationOpTranslationStrategy()
        ));

        this.map.put(EntityFilterOp.class, Collections.singletonList(
                new EntityFilterOpTranslationStrategy()
        ));

        this.map.put(RelationFilterOp.class, Collections.singletonList(
                new RelationFilterOpTranslationStrategy()
        ));
    }
    //endregion


    public GraphTraversal<Element, Path>  translate(Plan plan, GraphTraversal graphTraversal, Ontology ontology) {
        AtomicReference<GraphTraversal> traversalReference = new AtomicReference<>(graphTraversal);
        // Create initial traversal
        Stream.ofAll(plan.getOps()).forEach(op ->
                map.get(op.getClass()).forEach(strategy -> {
                    traversalReference.set(strategy.apply(new TranslationStrategyContext( op, plan, ontology), traversalReference.get()));
                }));

        return traversalReference.get().path();
    }
}
