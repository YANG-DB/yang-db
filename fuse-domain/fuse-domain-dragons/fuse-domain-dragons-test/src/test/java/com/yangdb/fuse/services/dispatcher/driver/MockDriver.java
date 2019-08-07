package com.yangdb.fuse.services.dispatcher.driver;

import com.google.inject.Inject;
import com.yangdb.fuse.client.export.GraphWriter;
import com.yangdb.fuse.client.export.GraphWriterStrategy;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.dispatcher.driver.*;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.dispatcher.resource.CursorResource;
import com.yangdb.fuse.dispatcher.resource.PageResource;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.dispatcher.validation.QueryValidator;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.query.QueryMetadata;
import com.yangdb.fuse.model.results.QueryResultBase;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import com.yangdb.fuse.model.transport.cursor.LogicalGraphCursorRequest;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Map;
import java.util.Optional;

/**
 * Created by Roman on 12/15/2017.
 */
public class MockDriver {
    public static class Query extends QueryDriverBase {
        //region Constructors
        @Inject
        public Query(
                CursorDriver cursorDriver,
                PageDriver pageDriver,
                QueryTransformer<com.yangdb.fuse.model.query.Query, AsgQuery> queryTransformer,
                QueryTransformer<String, AsgQuery> queryJasonTransformer,
                QueryValidator<AsgQuery> queryValidator,
                ResourceStore resourceStore,
                AppUrlSupplier urlSupplier) {
            super(cursorDriver, pageDriver, queryTransformer, queryJasonTransformer, queryValidator, resourceStore, urlSupplier);
        }
        //endregion

        //region QueryDriverBase Implementation
        @Override
        protected QueryResource createResource(CreateQueryRequest request, com.yangdb.fuse.model.query.Query query, AsgQuery asgQuery, QueryMetadata metadata) {
            return new QueryResource(request, query, asgQuery, metadata, new PlanWithCost<>(new Plan(), new PlanDetailedCost()), Optional.empty());
        }

        @Override
        protected AsgQuery rewrite(AsgQuery asgQuery) {
            return asgQuery;
        }

        @Override
        public Optional<Object> run(com.yangdb.fuse.model.query.Query query) {
            return Optional.empty();
        }

        @Override
        public Optional<Object> run(String cypher, String ontology) {
            return Optional.empty();
        }

        @Override
        public Optional<GraphTraversal> traversal(com.yangdb.fuse.model.query.Query query) {
            return Optional.empty();
        }

        @Override
        protected PlanWithCost<Plan, PlanDetailedCost> planWithCost(QueryMetadata metadata, AsgQuery query) {
            return PlanWithCost.EMPTY_PLAN;
        }
        //endregion
    }

    public static class Cursor extends CursorDriverBase {
        //region Constructors
        @Inject
        public Cursor(ResourceStore resourceStore, PageDriver pageDriver,
                      AppUrlSupplier urlSupplier, CursorFactory cursorFactory) {
            super(resourceStore, urlSupplier);
            this.pageDriver = pageDriver;
            this.cursorFactory = cursorFactory;
        }
        //endregion

        //region CursorDriverBase Implementation
        @Override
        protected CursorResource createResource(QueryResource queryResource, String cursorId, CreateCursorRequest cursorRequest) {
            return new CursorResource(
                    cursorId,
                    cursorFactory.createCursor(
                            new CursorFactory.Context.Impl(
                                    queryResource,
                                    cursorRequest)),
                    cursorRequest);
        }
        //endregion

        //region Fields
        private CursorFactory cursorFactory;


        @Override
        public Optional<GraphTraversal> traversal(PlanWithCost plan, String ontology) {
            return Optional.empty();
        }
        //endregion
    }

    public static class Page extends PageDriverBase {
        //region Constructors
        @Inject
        public Page(ResourceStore resourceStore, AppUrlSupplier urlSupplier, GraphWriterStrategy writerMap) {
            super(resourceStore, urlSupplier,writerMap);
        }
        //endregion

        //region PageDriverBase Implementation
        @Override
        protected PageResource<QueryResultBase> createResource(QueryResource queryResource, CursorResource cursorResource, String pageId, int pageSize) {
            QueryResultBase assignmentsQueryResult = cursorResource.getCursor().getNextResults(pageSize);
            return new PageResource<>(pageId, assignmentsQueryResult, pageSize, 0);
        }

        //endregion
    }
}
