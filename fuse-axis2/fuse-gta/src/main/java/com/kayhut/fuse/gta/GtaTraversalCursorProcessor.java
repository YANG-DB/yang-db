package com.kayhut.fuse.gta;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.LoggerAnnotation;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.executor.ontology.UniGraphProvider;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.gta.translation.PlanTraversalTranslator;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Element;

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
            OntologyProvider provider,
            PlanTraversalTranslator planTraversalTranslator,
            CursorFactory cursorFactory,
            UniGraphProvider uniGraphProvider) {
        this.eventBus = eventBus;
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
    @LoggerAnnotation(name = "process", options = LoggerAnnotation.Options.full, logLevel = Slf4jReporter.LoggingLevel.DEBUG)
    public CursorCreationOperationContext process(CursorCreationOperationContext context) throws Exception {
        if (context.getCursor() != null) {
            return context;
        }
        Timer.Context time = metricRegistry.timer(
                name(QueryCreationOperationContext.class.getSimpleName(),
                        context.getQueryResource().getQueryMetadata().getId(),
                        GtaTraversalCursorProcessor.class.getSimpleName())).time();

        //execute gta plan ==> traversal extraction
        PlanWithCost<Plan, PlanDetailedCost> executionPlan = context.getQueryResource().getExecutionPlan();
        Ontology ontology = provider.get(context.getQueryResource().getQuery().getOnt()).get();

        GraphTraversal<?, ?> traversal  = this.planTraversalTranslator.translate(
                executionPlan.getPlan(),
                new TranslationContext(
                        new Ontology.Accessor(ontology),
                        uniGraphProvider.getGraph(ontology).traversal()));

        //submit
        Cursor cursor = this.cursorFactory.createCursor(
                new TraversalCursorContext(
                        ontology,
                        context.getQueryResource(),
                        context.getCursorType(),
                        traversal.path()));
        time.stop();
        return submit(eventBus, context.of(cursor));

    }
    //endregion

    //region Fields
    private final EventBus eventBus;
    private OntologyProvider provider;
    private PlanTraversalTranslator planTraversalTranslator;
    private CursorFactory cursorFactory;
    private UniGraphProvider uniGraphProvider;
    //endregion
}
