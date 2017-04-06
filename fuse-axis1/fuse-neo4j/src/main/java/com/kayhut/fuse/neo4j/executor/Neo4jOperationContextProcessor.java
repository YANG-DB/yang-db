package com.kayhut.fuse.neo4j.executor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.costs.SingleCost;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import javaslang.Tuple2;

import java.util.Optional;

import static com.kayhut.fuse.model.Utils.submit;
import static com.kayhut.fuse.model.results.QueryResult.QueryResultBuilder.aQueryResult;

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

        return submit(eventBus, context.of(new Tuple2<>(new Plan(), new SingleCost(0.0))));
    }
    //endregion

    //region CursorCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public CursorCreationOperationContext process(CursorCreationOperationContext context) {
        if (context.getCursor() != null) {
            return context;
        }

        //Compile the query and get the cursor ready
        String cypherQuery = null;
        try {
            cypherQuery = CypherCompiler.compile(
                    context.getQueryResource().getAsgQuery(),
                    ontologyProvider.get(context.getQueryResource().getQuery().getOnt()).get());
        } catch (Exception ex) {

        }

        Cursor cursor = this.cursorFactory.createCursor(new Neo4jCursorFactory.Neo4jCursorContext(context.getQueryResource(), null));

        return submit(eventBus, context.of(cursor));

    }
    //endregion

    //region PageCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public PageCreationOperationContext process(PageCreationOperationContext context) {
        if (context.getPageResource() != null) {
            return context;
        }

        Cursor cursor = context.getCursorResource().getCursor();
        QueryResult result = cursor.getNextResults(context.getPageSize());

        if (result == null) {
            result = aQueryResult().build();
        }

        return submit(eventBus, context.of(new PageResource(context.getPageId(), result, context.getPageSize())));
    }
    //endregion

    //region Fields
    protected EventBus eventBus;
    private ResourceStore store;
    private OntologyProvider ontologyProvider;
    private CursorFactory cursorFactory;
    //endregion
}
