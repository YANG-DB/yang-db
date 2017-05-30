package com.kayhut.fuse.neo4j.executor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;

import java.util.Optional;

import static com.kayhut.fuse.model.Utils.submit;

/**
 * Created by User on 08/03/2017.
 */
public class Neo4jOperationContextProcessor implements
        CursorCreationOperationContext.Processor,
        PageCreationOperationContext.Processor,
        QueryCreationOperationContext.Processor {

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

        Optional<Ontology> ont = ontologyProvider.get(context.getAsgQuery().getOnt());
        if(!ont.isPresent()) {
            throw new RuntimeException("Query ontology not present in catalog.");
        }

        PlanWithCost<Plan, PlanDetailedCost> planWithCost = new PlanWithCost<>(new Plan(), new PlanDetailedCost());
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

        //Compile the query and get the cursor ready
        String cypherQuery;
        try {
            cypherQuery = CypherCompiler.compile(
                    context.getQueryResource().getAsgQuery(),
                    ontologyProvider.get(context.getQueryResource().getQuery().getOnt()).get());
        } catch (Exception ex) {
            throw new RuntimeException("Failed parsing cypher query " + ex);
        }

        Cursor cursor = this.cursorFactory.createCursor(new Neo4jCursorFactory.Neo4jCursorContext(context.getQueryResource(), cypherQuery));

        return submit(eventBus, context.of(cursor));

    }
    //endregion

    //region PageCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public PageCreationOperationContext process(PageCreationOperationContext context) {

        //TODO: called twice !!

        if (context.getPageResource() == null || context.getPageResource().getData() == null) {
            QueryResult queryResult = context.getCursorResource().getCursor().getNextResults(context.getPageSize());
            context = context.of(new PageResource(context.getPageId(), queryResult, context.getPageSize()));
            submit(eventBus, context);
        }

        return context;

    }
    //endregion

    //region Fields
    protected EventBus eventBus;
    private ResourceStore store;
    private OntologyProvider ontologyProvider;
    private CursorFactory cursorFactory;
    //endregion
}
