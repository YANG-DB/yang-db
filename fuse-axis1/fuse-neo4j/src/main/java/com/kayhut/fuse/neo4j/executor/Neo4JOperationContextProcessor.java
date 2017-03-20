package com.kayhut.fuse.neo4j.executor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.neo4j.GraphProvider;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;

import java.util.Optional;

import static com.kayhut.fuse.model.Utils.submit;
import static com.kayhut.fuse.neo4j.executor.NeoGraphUtils.query;

/**
 * Created by User on 08/03/2017.
 */
public class Neo4JOperationContextProcessor implements
        QueryCreationOperationContext.Processor,
        CursorCreationOperationContext.Processor,
        PageCreationOperationContext.Processor,
        QueryCreationOperationContext.Processor {

    //region Constructors
    @Inject
    public Neo4JOperationContextProcessor(GraphProvider graphProvider, EventBus eventBus, ResourceStore store, OntologyProvider provider) {
        this.graphProvider = graphProvider;
        this.eventBus = eventBus;
        this.store = store;
        this.provider = provider;
        this.eventBus.register(this);
    }
    //endregion

    //region QueryCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public QueryCreationOperationContext process(QueryCreationOperationContext context) {

        if(context.getAsgQuery() == null) {
            return context;
        }

        asgQuery = context.getAsgQuery();
        Optional<Ontology> ont = provider.get(asgQuery.getOnt());
        if(ont.isPresent()) {
            ontology = ont.get();
        } else {
            throw new RuntimeException("Query ontology not present in catalog.");
        }

        return context;
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
        String cypherQuery = CypherCompiler.compile(asgQuery,ontology);

        return submit(eventBus, context.of(new Neo4jCursor(context.getQueryResource().getQuery(), cypherQuery)));

    }
    //endregion

    //region PageCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public PageCreationOperationContext process(PageCreationOperationContext context) {
        if (context.getPageResource() != null) {
            return context;
        }

        Neo4jCursor neo4jCursor = (Neo4jCursor)context.getCursorResource().getCursor();
        QueryResult result = query(graphProvider,neo4jCursor);
        if (result == null) {
            result = new QueryResult();
        }

        return submit(eventBus, context.of(new PageResource(context.getPageId(), result, context.getPageSize())));
    }
    //endregion

    private GraphProvider graphProvider;
    //region Fields
    protected EventBus eventBus;
    private ResourceStore store;
    private OntologyProvider provider;
    private AsgQuery asgQuery;
    private Ontology ontology;

    @Override
    @Subscribe
    public QueryCreationOperationContext process(QueryCreationOperationContext context) {
        AsgQuery asgQuery = context.getAsgQuery();
        System.out.println(asgQuery);
        return context;
    }

    //endregion
}
