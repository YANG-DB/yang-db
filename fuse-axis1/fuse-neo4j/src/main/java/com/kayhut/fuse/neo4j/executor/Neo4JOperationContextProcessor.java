package com.kayhut.fuse.neo4j.executor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.ResourceStore;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.neo4j.GraphProvider;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import com.kayhut.fuse.neo4j.cypher.CypherCompilerException;

import java.util.Optional;

import static com.kayhut.fuse.model.Utils.submit;
import static com.kayhut.fuse.neo4j.executor.NeoGraphUtils.query;

/**
 * Created by User on 08/03/2017.
 */
public class Neo4JOperationContextProcessor implements
        CursorCreationOperationContext.Processor,
        PageCreationOperationContext.Processor {

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

    //region CursorCreationOperationContext.Processor Implementation
    @Override
    @Subscribe
    public CursorCreationOperationContext process(CursorCreationOperationContext context) {
        if (context.getCursor() != null) {
            return context;
        }

        // TODO: use ASG
        // AsgQuery asgQuery = input.getAsgQuery();

        Query query = context.getQueryResource().getQuery();
        Optional<Ontology> ontology = provider.get(query.getOnt());
        if(!ontology.isPresent())
            throw new RuntimeException("Ontology "+query.getOnt() + " not present in catalog ");

        try {
            String cypherQuery = CypherCompiler.compile(query, ontology.get());
            return submit(eventBus, context.of(new Neo4jCursor(context.getQueryResource().getQuery(),cypherQuery)));
        } catch (CypherCompilerException e) {
            throw new RuntimeException(e);
        }

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

    //endregion
}
