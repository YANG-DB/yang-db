package com.kayhut.fuse.neo4j.executor;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;

import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by User on 08/03/2017.
 */
/*public class Neo4jOperationContextProcessor implements
        CursorCreationOperationContext.Processor,
        QueryCreationOperationContext.Processor {

    @Inject
    private MetricRegistry metricRegistry;

    //region Constructors
    @Inject
    public Neo4jOperationContextProcessor(
            ResourceStore resourceStore,
            OntologyProvider ontologyProvider,
            CursorFactory cursorFactory) {
        this.cursorFactory = cursorFactory;
        this.resourceStore = resourceStore;
        this.ontologyProvider = ontologyProvider;
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
        return context.of(planWithCost);
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
                        context.getQueryId(),
                        Neo4jOperationContextProcessor.class.getSimpleName())).time();

        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(context.getQueryId());
        if (!queryResource.isPresent()) {
            // maybe the resource was deleted so bail early
            return context;
        }

        //Compile the query and get the cursor ready
        String cypherQuery;
        try {
            cypherQuery = CypherCompiler.compile(
                    queryResource.get().getAsgQuery(),
                    ontologyProvider.get(queryResource.get().getQuery().getOnt()).get());
        } catch (Exception ex) {
            throw new RuntimeException("Failed parsing cypher query " + ex);
        }

        Cursor cursor = this.cursorFactory.createCursor(
                new Neo4jCursorFactory.Neo4jCursorContext(queryResource.get(),
                                                          cypherQuery,
                                                          ontologyProvider.get(queryResource.get().getQuery().getOnt()).get()));

        time.stop();

        return context.of(cursor);

    }
    //endregion

    //region Fields
    private ResourceStore resourceStore;
    private OntologyProvider ontologyProvider;
    private CursorFactory cursorFactory;
    //endregion
}*/
