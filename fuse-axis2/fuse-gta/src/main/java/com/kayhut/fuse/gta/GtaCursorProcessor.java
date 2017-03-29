package com.kayhut.fuse.gta;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;
import com.kayhut.fuse.model.ontology.Ontology;
import javaslang.Tuple2;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by liorp on 3/16/2017.
 */
public class GtaCursorProcessor implements
        CursorCreationOperationContext.Processor {

    private final EventBus eventBus;
    private OntologyProvider provider;
    private GremlinTranslationAppenderEngine engine;

    @Inject
    public GtaCursorProcessor(EventBus eventBus, OntologyProvider provider, GremlinTranslationAppenderEngine engine) {
        this.eventBus = eventBus;
        this.provider = provider;
        this.engine = engine;
        this.eventBus.register(this);

    }

    @Override
    @Subscribe
    public CursorCreationOperationContext process(CursorCreationOperationContext context) {
        if (context.getCursor() != null) {
            return context;
        }
        //execute gta plan ==> traversal extraction
        Tuple2<Plan, SingleCost> executionPlan = context.getQueryResource().getExecutionPlan();
        Ontology ontology = provider.get(context.getQueryResource().getQuery().getOnt()).get();
        Traversal traversal = engine.createTraversal(ontology, executionPlan._1());
        //submit
        return submit(eventBus, context.of(new TraversalCursor(traversal)));

    }

}
