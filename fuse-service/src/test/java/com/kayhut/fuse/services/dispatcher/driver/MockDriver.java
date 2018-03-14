package com.kayhut.fuse.services.dispatcher.driver;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.driver.CursorDriverBase;
import com.kayhut.fuse.dispatcher.driver.PageDriverBase;
import com.kayhut.fuse.dispatcher.driver.QueryDriverBase;
import com.kayhut.fuse.dispatcher.query.QueryTransformer;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.PageResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.validation.QueryValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;

import java.util.Optional;

/**
 * Created by Roman on 12/15/2017.
 */
public class MockDriver {
    public static class Query extends QueryDriverBase {
        //region Constructors
        @Inject
        public Query(QueryTransformer<com.kayhut.fuse.model.query.Query, AsgQuery> queryTransformer, QueryValidator<AsgQuery> queryValidator, ResourceStore resourceStore, AppUrlSupplier urlSupplier) {
            super(queryTransformer, queryValidator, resourceStore, urlSupplier);
        }
        //endregion

        //region QueryDriverBase Implementation
        @Override
        protected QueryResource createResource(com.kayhut.fuse.model.query.Query query, AsgQuery asgQuery, QueryMetadata metadata) {
            return new QueryResource(query, asgQuery, metadata, new PlanWithCost<>(new Plan(), new PlanDetailedCost()), Optional.empty());
        }
        //endregion
    }

    public static class Cursor extends CursorDriverBase {
        //region Constructors
        @Inject
        public Cursor(ResourceStore resourceStore, AppUrlSupplier urlSupplier, CursorFactory cursorFactory) {
            super(resourceStore, urlSupplier);
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
        //endregion
    }

    public static class Page extends PageDriverBase {
        //region Constructors
        @Inject
        public Page(ResourceStore resourceStore, AppUrlSupplier urlSupplier) {
            super(resourceStore, urlSupplier);
        }
        //endregion

        //region PageDriverBase Implementation
        @Override
        protected PageResource<QueryResult> createResource(QueryResource queryResource, CursorResource cursorResource, String pageId, int pageSize) {
            QueryResult queryResult = cursorResource.getCursor().getNextResults(pageSize);
            return new PageResource<>(pageId, queryResult, pageSize, 0);
        }
        //endregion
    }
}
