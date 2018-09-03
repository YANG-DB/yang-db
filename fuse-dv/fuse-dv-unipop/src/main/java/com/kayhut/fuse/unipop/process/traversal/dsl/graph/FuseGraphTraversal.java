package com.kayhut.fuse.unipop.process.traversal.dsl.graph;

import com.kayhut.fuse.unipop.process.traversal.traverser.ThinPathTraverserGenerator;
import com.kayhut.fuse.unipop.process.traversal.traverser.ThinPathTraverserGeneratorFactory;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.TraverserGenerator;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import com.kayhut.fuse.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.branch.ChooseStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.branch.UnionStep;
import org.apache.tinkerpop.gremlin.process.traversal.util.DefaultTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * Created by Roman on 1/29/2018.
 */
public class FuseGraphTraversal<S, E> extends DefaultTraversal<S, E> implements GraphTraversal.Admin<S, E> {
    //region Constructors
    public FuseGraphTraversal() {
    }

    public FuseGraphTraversal(GraphTraversalSource graphTraversalSource) {
        super(graphTraversalSource);

        this.getSideEffects().setSack(
                () -> (Map<String, Object>)new HashMap<String, Object>(),
                HashMap::new,
                (sack1, sack2) -> {
                    Map<String, Object> mergedSack = new HashMap<>(sack1);
                    mergedSack.putAll(sack2);
                    return mergedSack;
                });
    }

    public FuseGraphTraversal(Graph graph) {
        super(graph);
    }
    //endregion

    //region DefaultTraversal Implementation
    @Override
    public TraverserGenerator getTraverserGenerator() {
        if (this.generator == null) {
            this.generator = new ThinPathTraverserGeneratorFactory().getTraverserGenerator(Collections.emptySet());
        }
        return this.generator;
    }

    @Override
    public <E2> GraphTraversal<S, E2> optional(final Traversal<?, E2> optionalTraversal) {
        this.asAdmin().getBytecode().addStep("optional", new Object[]{optionalTraversal});
        return this.asAdmin().addStep(new ChooseStep(
                this.asAdmin(),
                (org.apache.tinkerpop.gremlin.process.traversal.Traversal.Admin)optionalTraversal,
                optionalTraversal.asAdmin().clone(),
                (org.apache.tinkerpop.gremlin.process.traversal.Traversal.Admin)__.start().identity()));
    }

//endregion

    //region GraphTraversal.Admin Implementation
    @Override
    public GraphTraversal.Admin<S, E> asAdmin() {
        return this;
    }

    @Override
    public GraphTraversal<S, E> iterate() {
        return (GraphTraversal<S, E>)super.iterate();
    }

    @Override
    public FuseGraphTraversal<S, E> clone() {
        return (FuseGraphTraversal) super.clone();
    }
    //endregion
}
