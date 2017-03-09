package com.kayhut.fuse.gta.translation;

import com.kayhut.fuse.gta.strategy.EntityOpAdjcentTranslationStrategy;
import com.kayhut.fuse.gta.strategy.EntityOpPostRelTranslationStrategy;
import com.kayhut.fuse.gta.strategy.EntityOpStartTranslationStrategy;
import com.kayhut.fuse.gta.strategy.RelationOpTranslationStrategy;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.unipop.PromiseGraph;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

/**
 * Created by moti on 3/7/2017.
 */
public class SimplePlanOpTranslator {

    //endregion

    //region Fields
    private Map<Class, List<BiFunction<Tuple2<Plan, PlanOpBase>, GraphTraversal, GraphTraversal>>> map;
    //endregion


    public SimplePlanOpTranslator(PromiseGraph promiseGraph) {
        this.map = new HashMap<>();

        //entity operations types list
        List<BiFunction<Tuple2<Plan, PlanOpBase>, GraphTraversal, GraphTraversal>> operations = new ArrayList<>();
        operations.add((context, traversal) -> new EntityOpStartTranslationStrategy(promiseGraph).apply(context, traversal));
        operations.add((context, traversal) -> new EntityOpAdjcentTranslationStrategy().apply(context, traversal));
        operations.add((context, traversal) -> new EntityOpPostRelTranslationStrategy().apply(context, traversal));

        this.map.put(EntityOp.class, operations);

        //relations operations map
        this.map.put(RelationOp.class, Collections.singletonList((context, traversal) -> new RelationOpTranslationStrategy().apply(context, traversal)));
    }

    public GraphTraversal translate(Plan plan, GraphTraversal graphTraversal) {
        AtomicReference<GraphTraversal> traversal = new AtomicReference<>(graphTraversal);
        // Create initial traversal
        Stream.ofAll(plan.getOps()).forEach(op ->
                map.get(op.getClass()).forEach(v -> traversal.set(v.apply(new Tuple2<>(plan, op), traversal.get()))));

    // iterate ops
    // translate each op via factory

        return traversal.get();
}
}
