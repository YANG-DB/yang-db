package com.yangdb.fuse.dispatcher.driver.execute;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.driver.CursorDriver;
import com.yangdb.fuse.dispatcher.driver.PageDriver;
import com.yangdb.fuse.dispatcher.epb.PlanSearcher;
import com.yangdb.fuse.dispatcher.query.JsonQueryTransformerFactory;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.dispatcher.resource.CursorResource;
import com.yangdb.fuse.dispatcher.resource.PageResource;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.dispatcher.validation.QueryValidator;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.query.ParameterizedQuery;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.NamedParameter;
import com.yangdb.fuse.model.query.properties.constraint.ParameterizedConstraint;
import com.yangdb.fuse.model.query.properties.constraint.QueryNamedParameter;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.resourceInfo.PageResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.results.AssignmentUtils;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequestMetadata;
import com.yangdb.fuse.model.transport.ExecuteStoredQueryRequest;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import javaslang.collection.Stream;
import javaslang.control.Option;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.yangdb.fuse.dispatcher.driver.execute.QueryExecutionUtils.ContinueOrBreakChain.BREAK;
import static com.yangdb.fuse.dispatcher.driver.execute.QueryExecutionUtils.ContinueOrBreakChain.CONTINUE;

public class StoredQueryDriverStrategy extends V1QueryDriverStrategy {

    @Inject
    public StoredQueryDriverStrategy(
                                    PageDriver pageDriver,
                                    CursorDriver cursorDriver,
                                     QueryTransformer<AsgQuery, AsgQuery> queryRewriter,
                                    QueryTransformer<Query, AsgQuery> queryTransformer,
                                     QueryValidator<AsgQuery> queryValidator,
                                     ResourceStore resourceStore,
                                     PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher,
                                     AppUrlSupplier urlSupplier) {
        super(pageDriver,cursorDriver, queryRewriter, queryTransformer, queryValidator, resourceStore, planSearcher, urlSupplier);
    }

    @Override
    public boolean test(CreateQueryRequest request) {
        return request.getType().equals(CreateQueryRequestMetadata.TYPE_V1_QUERY) && (request instanceof ExecuteStoredQueryRequest);

    }

    public Optional<QueryResourceInfo> call(ExecuteStoredQueryRequest callRequest) {
        try {
            if (!resourceStore.getQueryResource(callRequest.getQuery().getName()).isPresent())
                return Optional.of(new QueryResourceInfo().error(
                        new FuseError(Query.class.getSimpleName(),
                                "Query with id[" + callRequest.getQuery().getName() + "] not found in store")));

            QueryResource queryResource = resourceStore.getQueryResource(callRequest.getQuery().getName()).get();
            final CreateQueryRequest storedRequest = queryResource.getRequest();

            //get cursor request - letting the calling request override the sored page request
            CreateCursorRequest cursorRequest = (callRequest.getCreateCursorRequest() != null
                    ? callRequest.getCreateCursorRequest()
                    : storedRequest.getCreateCursorRequest());

            //get page request - letting the calling request override the sored page request
            CreatePageRequest pageRequest = (callRequest.getPageCursorRequest() != null
                    ? callRequest.getPageCursorRequest()
                    : (storedRequest.getCreateCursorRequest() != null
                    ? storedRequest.getCreateCursorRequest().getCreatePageRequest()
                    : new CreatePageRequest()));

            //set pageSize attribute on PageCursorRequest using the given execution params
            callRequest.getExecutionParams().stream().filter(p -> p.getName().equals("pageSize")).findAny()
                    .ifPresent(v -> pageRequest.setPageSize((Integer) v.getValue()));

            //create the new volatile query
            Optional<QueryResourceInfo> info = create(new CreateQueryRequest(
                    callRequest.getId(),
                    callRequest.getName(),
                    new ParameterizedQuery(queryResource.getQuery(), callRequest.getParameters()),
                    callRequest.getPlanTraceOptions(),
                    cursorRequest.with(pageRequest)));
            //remove volatile query after execution returns result - should this be done right away since more pages can be requested ...
            //resourceStore.deleteQueryResource(callRequest.getId());
            return info;
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),err)));
        }
    }

    /**
     * handle parameterized query ->
     * this will eventually call this create() method with the real parameterized concrete values
     *
     * @param request
     * @param queryResourceInfo
     * @return
     */
    private Optional<QueryResourceInfo> parameterizedQuery(CreateQueryRequestMetadata request, Optional<QueryResourceInfo> queryResourceInfo) {
        if (queryResourceInfo.get().getType() == CreateQueryRequestMetadata.QueryType.parameterized) {
            Optional<QueryResourceInfo> resourceInfo = call(new ExecuteStoredQueryRequest(
                    "call[" + request.getId() + "]",
                    request.getId(),
                    request.getCreateCursorRequest(),
                    extractInnerQueryParams(queryResourceInfo.get()),
                    Collections.emptyList()
            ));
            //return the called query call_[***] instead of the origin ***
            if (resourceInfo.isPresent()) {
                return resourceInfo;
            }
        }
        return Optional.empty();
    }

    private Collection<NamedParameter> extractInnerQueryParams(QueryResourceInfo queryResourceInfo) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryResourceInfo.getResourceId());
        List<EProp> parameterizedConstraints = AsgQueryUtil.getParameterizedConstraintEProps(queryResource.get().getAsgQuery());
        return parameterizedConstraints.stream()
                .map(eProp -> extractQueryProjectedParams(queryResource.get(), (ParameterizedConstraint) eProp.getCon()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<NamedParameter> extractQueryProjectedParams(QueryResource queryResource, ParameterizedConstraint con) {
        QueryNamedParameter namedParameter = (QueryNamedParameter) con.getParameter();
        String query = namedParameter.getQuery();
        Option<QueryResource> innerQuery = Stream.ofAll(queryResource.getInnerQueryResources())
                .find(p -> p.getQuery().getName().contains(query));

        if (!innerQuery.isEmpty()) {
            CursorResource cursorResource = innerQuery.get().getCursorResource(innerQuery.get().getCurrentCursorId()).get();
            PageResource pageResource = cursorResource.getPageResource(cursorResource.getCurrentPageId()).get();
            AssignmentsQueryResult result = (AssignmentsQueryResult) pageResource.getData();
            return Optional.of(AssignmentUtils.collectByTag(result, namedParameter.getName()));
        }

        return Optional.empty();
    }

    @Override
    protected QueryPipeExecutionChain getExecutionChain() {
        return new QueryPipeExecutionChain() {
            @Override
            public ContinueOrBreakChain<Optional<QueryResourceInfo>> preChainStart(CreateQueryRequestMetadata request, Optional<QueryResourceInfo> queryResourceInfo) {
                return super.preChainStart(request, queryResourceInfo);
            }

            @Override
            protected ContinueOrBreakChain<Optional<QueryResourceInfo>> preCreateCursor(CreateQueryRequestMetadata request, Optional<QueryResourceInfo> queryResourceInfo) {
                return super.preCreateCursor(request, queryResourceInfo);
            }

            @Override
            protected ContinueOrBreakChain<Optional<QueryResourceInfo>> postCreateCursor(CreateQueryRequestMetadata request, Optional<CursorResourceInfo> cursorResourceInfo, Optional<QueryResourceInfo> queryResourceInfo) {
                return super.postCreateCursor(request, cursorResourceInfo, queryResourceInfo);
            }

            @Override
            protected ContinueOrBreakChain<Optional<QueryResourceInfo>> preCreatePage(CreateQueryRequestMetadata request, Optional<CursorResourceInfo> cursorResourceInfo, Optional<QueryResourceInfo> queryResourceInfo) {
                return super.preCreatePage(request, cursorResourceInfo, queryResourceInfo);
            }

            @Override
            protected ContinueOrBreakChain<Optional<QueryResourceInfo>> postCreatePage(CreateQueryRequestMetadata request, Optional<PageResourceInfo> pageResourceInfo, Optional<CursorResourceInfo> cursorResourceInfo, Optional<QueryResourceInfo> queryResourceInfo) {
                return super.postCreatePage(request, pageResourceInfo, cursorResourceInfo, queryResourceInfo);
            }

            @Override
            protected ContinueOrBreakChain<Optional<QueryResourceInfo>> preGetData(CreateQueryRequestMetadata request, Optional<PageResourceInfo> pageResourceInfo, Optional<CursorResourceInfo> cursorResourceInfo, Optional<QueryResourceInfo> queryResourceInfo) {
                return super.preGetData(request, pageResourceInfo, cursorResourceInfo, queryResourceInfo);
            }

            @Override
            protected ContinueOrBreakChain<Optional<QueryResourceInfo>> postGetData(CreateQueryRequestMetadata request, Optional<Object> pageDataResponse, Optional<PageResourceInfo> pageResourceInfo, Optional<CursorResourceInfo> cursorResourceInfo, Optional<QueryResourceInfo> queryResourceInfo) {
                ContinueOrBreakChain<Optional<QueryResourceInfo>> chain = super.postGetData(request, pageDataResponse, pageResourceInfo, cursorResourceInfo, queryResourceInfo);
                if(chain.isBreakChain())
                    return chain;

                //handle parameterized query -> will eventually call this getQueryResourceInfo() method with the real parameterized concrete values
                Optional<QueryResourceInfo> resourceInfo = parameterizedQuery(request, queryResourceInfo);
                if (resourceInfo.isPresent()) {
                    String innerQueryResourceId = queryResourceInfo.get().getInnerUrlResourceInfos().get(0).getResourceId();
                    Optional<QueryResourceInfo> info = getInfo(innerQueryResourceId);
                    resourceInfo.get().withInnerQueryResources(Collections.singletonList(info.get()));
                    return BREAK(resourceInfo);
                }
                return CONTINUE();
            }
        };
    }
}
