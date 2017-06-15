package com.kayhut.fuse.neo4j.executor;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;

import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by User on 08/03/2017.
 */
public class Neo4jOperationContextProcessor implements
        CursorCreationOperationContext.Processor,
        QueryCreationOperationContext.Processor {

    @Inject
    private MetricRegistry metricRegistry;

    //region Constructors
    @Inject
    public Neo4jOperationContextProcessor(
            EventBus eventBus,
            ResourceStore store,
            OntologyProvider ontologyProvider,
            CursorFactory cursorFactory) {
        this.cursorFactory = cursorFactory;
        this.eventBus = eventBus;
        this.store = store;
        this.ontologyProvider = ontologyProvider;
        this.eventBus.register(this);
    }
    //endregion

    //region QueryCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public QueryCreationOperationContext process(QueryCreationOperationContext context) {
        if(context.getAsgQuery() == null || context.getExecutionPlan() != null) {
            return context;
        }

        Timer.Context time = metricRegistry.timer(
                name(QueryCreationOperationContext.class.getSimpleName(),
                        context.getQueryMetadata().getId(),
                        Neo4jOperationContextProcessor.class.getSimpleName())).time();

        Optional<Ontology> ont = ontologyProvider.get(context.getAsgQuery().getOnt());
        if(!ont.isPresent()) {
            throw new RuntimeException("Query ontology not present in catalog.");
        }

        PlanWithCost<Plan, PlanDetailedCost> planWithCost = new PlanWithCost<>(new Plan(), new PlanDetailedCost());

        time.stop();
        return submit(eventBus, context.of(planWithCost));
    }
    //endregion

    //region CursorCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public CursorCreationOperationContext process(CursorCreationOperationContext context) {

        if (context.getCursor() != null) {
            return context;
        }

        //TODO: called twice !!
        Timer.Context time = metricRegistry.timer(
                name(QueryCreationOperationContext.class.getSimpleName(),
                        context.getQueryResource().getQueryMetadata().getId(),
                        Neo4jOperationContextProcessor.class.getSimpleName())).time();

        //Compile the query and get the cursor ready
        String cypherQuery;
        try {
            cypherQuery = CypherCompiler.compile(
                    context.getQueryResource().getAsgQuery(),
                    ontologyProvider.get(context.getQueryResource().getQuery().getOnt()).get());
        } catch (Exception ex) {
            throw new RuntimeException("Failed parsing cypher query " + ex);
        }

        Cursor cursor = this.cursorFactory.createCursor(
                new Neo4jCursorFactory.Neo4jCursorContext(context.getQueryResource(),
                                                          cypherQuery,
                                                          ontologyProvider.get(context.getQueryResource().getQuery().getOnt()).get()));

        time.stop();

        return submit(eventBus, context.of(cursor));

    }
    //endregion

    //region Fields
    protected EventBus eventBus;
    private ResourceStore store;
    private OntologyProvider ontologyProvider;
    private CursorFactory cursorFactory;
    //endregion
}
