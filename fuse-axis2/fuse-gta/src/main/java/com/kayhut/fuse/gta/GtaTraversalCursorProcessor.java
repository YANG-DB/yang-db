package com.kayhut.fuse.gta;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.executor.ontology.UniGraphProvider;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by liorp on 3/16/2017.
 */
public class GtaTraversalCursorProcessor implements CursorCreationOperationContext.Processor {
    //region Constructors
    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    public GtaTraversalCursorProcessor(
            EventBus eventBus,
            ResourceStore resourceStore,
            OntologyProvider provider,
            PlanTraversalTranslator planTraversalTranslator,
            CursorFactory cursorFactory,
            UniGraphProvider uniGraphProvider) {
        this.eventBus = eventBus;
        this.resourceStore = resourceStore;
        this.provider = provider;
        this.planTraversalTranslator = planTraversalTranslator;
        this.cursorFactory = cursorFactory;
        this.uniGraphProvider = uniGraphProvider;
        this.eventBus.register(this);
    }
    //endregion

    //region CursorCreationOperationContext.Processor implementation
    @Override
    @Subscribe
    public CursorCreationOperationContext process(CursorCreationOperationContext context) throws Exception {
        if (context.getCursor() != null) {
            return context;
        }

        Timer.Context time = metricRegistry.timer(
                name(QueryCreationOperationContext.class.getSimpleName(),
                        context.getQueryId(),
                        GtaTraversalCursorProcessor.class.getSimpleName())).time();

        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(context.getQueryId());
        if (!queryResource.isPresent()) {
            // maybe the query was deleted so throw an error
            return context;
        }

        //execute gta plan ==> traversal extraction
        PlanWithCost<Plan, PlanDetailedCost> executionPlan = queryResource.get().getExecutionPlan();
        Ontology ontology = provider.get(queryResource.get().getQuery().getOnt()).get();

        GraphTraversal<?, ?> traversal = this.planTraversalTranslator.translate(
                    executionPlan,
                    new TranslationContext(
                            new Ontology.Accessor(ontology),
                            uniGraphProvider.getGraph(ontology).traversal()));

        //submit
        Cursor cursor = this.cursorFactory.createCursor(
                new TraversalCursorContext(
                        ontology,
                        queryResource.get(),
                        context.getCursorType(),
                        traversal.path()));
        time.stop();
        return submit(eventBus, context.of(cursor));

    }
    //endregion

    //region Fields
    private final EventBus eventBus;
    private ResourceStore resourceStore;
    private OntologyProvider provider;
    private PlanTraversalTranslator planTraversalTranslator;
    private CursorFactory cursorFactory;
    private UniGraphProvider uniGraphProvider;
    //endregion
}
