package com.kayhut.fuse.gta.translation;

import com.google.inject.Inject;
import com.kayhut.fuse.executor.uniGraphProvider.UniGraphProvider;
import com.kayhut.fuse.gta.strategy.*;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.ontology.Ontology;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.unipop.structure.UniGraph;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by moti on 3/7/2017.
 */
public class M1PlanTranslator implements PlanTranslator{
    //region Constructors
    @Inject
    public M1PlanTranslator(UniGraphProvider uniGraphProvider) {
        this.uniGraphProvider = uniGraphProvider;
    }
    //endregion

    //region PlanTranslator Implementation
    public Traversal<Element, Path> translate(Plan plan, Ontology ontology) throws Exception {
        TranslationStrategy translationStrategy = getTranslationStrategy(ontology);
        GraphTraversal traversal = __.start();
        TranslationStrategyContext context = new TranslationStrategyContext(plan, ontology);
        for (PlanOpBase planOp : plan.getOps()) {
            traversal = translationStrategy.translate(traversal, planOp, context);
        }

        return traversal.path();
    }
    //endregion

    //region Private Methods
    private TranslationStrategy getTranslationStrategy(Ontology ontology) throws Exception {
        UniGraph graph = this.uniGraphProvider.getGraph(ontology);

        return new CompositeTranslationStrategy(
                new EntityOpTranslationStrategy(graph),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new EntityFilterOpTranslationStrategy(),
                new RelationFilterOpTranslationStrategy()
        );
    }
    //endregion

    //region Fields
    private UniGraphProvider uniGraphProvider;
    //endregion
}
