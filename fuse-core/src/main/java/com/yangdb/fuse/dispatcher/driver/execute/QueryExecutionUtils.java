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
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.dispatcher.validation.QueryValidator;
import com.yangdb.fuse.model.asgQuery.AsgCompositeQuery;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryMetadata;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.resourceInfo.PageResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequestMetadata;
import com.yangdb.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.yangdb.fuse.dispatcher.driver.execute.QueryExecutionUtils.ContinueOrBreakChain.BREAK;
import static com.yangdb.fuse.dispatcher.driver.execute.QueryExecutionUtils.ContinueOrBreakChain.CONTINUE;
import static com.yangdb.fuse.model.Utils.getOrCreateId;
import static com.yangdb.fuse.model.asgQuery.AsgCompositeQuery.hasInnerQuery;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.QueryType.concrete;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.QueryType.parameterized;
import static java.util.Collections.EMPTY_LIST;

public abstract class QueryExecutionUtils<T extends CreateQueryRequestMetadata> implements QueryDriverStrategy<T> {
    private PageDriver pageDriver;
    protected final CursorDriver cursorDriver;
    protected final QueryTransformer<AsgQuery, AsgQuery> queryRewriter;
    protected final QueryValidator<AsgQuery> queryValidator;
    protected final ResourceStore resourceStore;
    protected final PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;
    protected final AppUrlSupplier urlSupplier;

    @Inject
    public QueryExecutionUtils(
            PageDriver pageDriver,
            CursorDriver cursorDriver,
            QueryTransformer<AsgQuery, AsgQuery> queryRewriter,
            QueryValidator<AsgQuery> queryValidator,
            ResourceStore resourceStore,
            PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher,
            AppUrlSupplier urlSupplier) {
        this.pageDriver = pageDriver;

        this.cursorDriver = cursorDriver;
        this.queryRewriter = queryRewriter;
        this.queryValidator = queryValidator;
        this.resourceStore = resourceStore;
        this.planSearcher = planSearcher;
        this.urlSupplier = urlSupplier;
    }

    protected QueryMetadata getQueryMetadata(T request) {
        String queryId = getOrCreateId(request.getId());
        return new QueryMetadata(request.getQueryType(), request.getStorageType(), queryId, request.getName(), request.isSearchPlan(), System.currentTimeMillis(), request.getTtl());
    }

    @Override
    public Optional<QueryResourceInfo> execute(T request, QueryMetadata metadata) {
        AsgQuery asgQuery = transform(request);
        asgQuery.setName(metadata.getName());
        asgQuery.setOnt(request.getOntology());
        //rewrite query
        asgQuery = rewrite(asgQuery);


        ValidationResult validationResult = validateAsgQuery(asgQuery);
        if (!validationResult.valid()) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),
                            validationResult.getValidator() + ":" + Arrays.toString(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)))));
        }

        //take ontology from AsgQuery since logical ontology can be deduced in the AsgStrategy pipe
        Query build = Query.Builder.instance()
//                    .withOnt(request.getOntology())
                .withOnt(asgQuery.getOnt())
                .withName(request.getName())
                .build();

        //create inner query
        final List<QueryResource> innerQuery = compositeQuery(request, metadata, asgQuery);

        //outer most query resource
        QueryResource queryResource = createResource(
                new CreateQueryRequest(request.getId(),
                        request.getName(),
                        build,
                        request.getPlanTraceOptions(),
                        request.getCreateCursorRequest())
                , build
                , asgQuery
                , metadata)
                .withInnerQueryResources(innerQuery);
        //add query resource to local query repository
        this.resourceStore.addQueryResource(queryResource);
        //return query resource info
        QueryResourceInfo queryResourceInfo = new QueryResourceInfo(
                metadata.getType(),
                urlSupplier.resourceUrl(metadata.getId()),
                metadata.getId(),
                urlSupplier.cursorStoreUrl(metadata.getId()))
                .withInnerQueryResources(getQueryResourceInfos(innerQuery));

        return Optional.of(queryResourceInfo);
    }


    public Optional<QueryResourceInfo> getInfo(String queryId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        //composite query info
        QueryResource resource = queryResource.get();
        final List<QueryResourceInfo> collect = Stream.ofAll(resource.getInnerQueryResources())
                .map(qr ->
                        new QueryResourceInfo(
                                qr.getQueryMetadata().getType(),
                                urlSupplier.resourceUrl(
                                        qr.getQueryMetadata().getId()),
                                qr.getQueryMetadata().getId(),
                                urlSupplier.cursorStoreUrl(qr.getQueryMetadata().getId()),
                                //inner cursor resource
                                cursorDriver.getInfo(qr.getQueryMetadata().getId(), qr.getCurrentCursorId()).isPresent() ?
                                        Collections.singletonList(
                                                cursorDriver.getInfo(qr.getQueryMetadata().getId(), qr.getCurrentCursorId()).get())
                                        : EMPTY_LIST))
                .toJavaList();

        QueryResourceInfo resourceInfo =
                new QueryResourceInfo(
                        resource.getQueryMetadata().getType(),
                        urlSupplier.resourceUrl(queryId),
                        queryId,
                        urlSupplier.cursorStoreUrl(queryId),
                        cursorDriver.getInfo(resource.getQueryMetadata().getId(), resource.getCurrentCursorId()).isPresent() ?
                                Collections.singletonList(
                                        cursorDriver.getInfo(resource.getQueryMetadata().getId(), resource.getCurrentCursorId()).get())
                                : EMPTY_LIST)
                        .withInnerQueryResources(collect);
        return Optional.of(resourceInfo);
    }

    protected List<QueryResourceInfo> getQueryResourceInfos(List<QueryResource> innerQuery) {
        return innerQuery.stream().map(qr -> new QueryResourceInfo(
                qr.getQueryMetadata().getType(),
                urlSupplier.resourceUrl(qr.getQueryMetadata().getId()),
                qr.getQueryMetadata().getId(),
                urlSupplier.cursorStoreUrl(qr.getQueryMetadata().getId())))
                .collect(Collectors.toList());
    }

    protected AsgQuery rewrite(AsgQuery asgQuery) {
        return this.queryRewriter.transform(asgQuery);
    }

    protected ValidationResult validateAsgQuery(AsgQuery query) {
        return this.queryValidator.validate(query);
    }

    /**
     * add inner query to repository with related parent query name
     *
     * @param request
     * @param metadata
     * @param outer
     */
    private List<QueryResource> compositeQuery(T request, QueryMetadata metadata, AsgQuery outer) {
        if (hasInnerQuery(outer)) {
            List<QueryResource> resources = ((AsgCompositeQuery) outer).getQueryChain().stream()
                    .map(inner -> getQueryResource(request, metadata.clone(), inner))
                    .collect(Collectors.toList());

            //unable to run plan search with QueryNamedParams due to DiscreteElementReduceController attempting to count elements...
            // this change is done only for the outer parameterized query
            metadata.setType(parameterized);
            metadata.setSearchPlan(false);
            return resources;
        }
        return Collections.emptyList();
    }

    /**
     * currently support only one level inner query hierarchy
     *
     * @param request
     * @param metadata
     * @param inner
     * @return
     */
    private QueryResource getQueryResource(T request, QueryMetadata metadata, AsgQuery inner) {
        ValidationResult validate = this.queryValidator.validate(inner);
        if (!validate.valid()) {
            throw new IllegalArgumentException(validate.toString());
        }
        //inner recursive query hierarchy - only one level hierarchy allowed
//        final List<QueryResource> innerQuery = compositeQuery(request, metadata, outer);

        Query query = inner.getOrigin();
        final QueryResource resource = createResource(
                new CreateQueryRequest(request.getId() + "->" + inner.getName(),
                        request.getName() + "->" + inner.getName(), query)
                , query
                , inner
                , new QueryMetadata(CreateQueryRequestMetadata.StorageType._volatile,
                        metadata.getId() + "->" + inner.getName(),
                        metadata.getName() + "->" + inner.getName(),
                        metadata.isSearchPlan(),
                        metadata.getCreationTime(),
                        metadata.getTtl()));
        this.resourceStore.addQueryResource(resource);
        return resource;
        //return query resource
    }

    protected Optional<QueryResourceInfo> getQueryResourceInfo(CreateQueryRequestMetadata request, Optional<QueryResourceInfo> queryResourceInfo) {
        //Chain of command - start
        ContinueOrBreakChain<Optional<QueryResourceInfo>> start = getExecutionChain().preChainStart(request,queryResourceInfo);
        if(start.isBreakChain())
            return start.getReturnOnBreak();

         //Chain of command - pre create cursor
        ContinueOrBreakChain<Optional<QueryResourceInfo>> preCreateCursor = getExecutionChain().preCreateCursor(request,queryResourceInfo);
        if(preCreateCursor.isBreakChain())
            return preCreateCursor.getReturnOnBreak();
        //create page
        Optional<CursorResourceInfo> cursorResourceInfo = this.cursorDriver.create(queryResourceInfo.get().getResourceId(), request.getCreateCursorRequest());
        //Chain of command - post create cursor
        ContinueOrBreakChain<Optional<QueryResourceInfo>> postCreateCursor = getExecutionChain().postCreateCursor(request,cursorResourceInfo, queryResourceInfo);
        if(postCreateCursor.isBreakChain())
            return postCreateCursor.getReturnOnBreak();

        //Chain of command- pre create driver
        ContinueOrBreakChain<Optional<QueryResourceInfo>> preCreateDriver = getExecutionChain().preCreatePage(request, cursorResourceInfo,queryResourceInfo);
        if(preCreateDriver.isBreakChain())
            return preCreateDriver.getReturnOnBreak();
        //create page
        Optional<PageResourceInfo> pageResourceInfo = this.pageDriver.create(
                queryResourceInfo.get().getResourceId(),
                cursorResourceInfo.get().getResourceId(),
                request.getCreateCursorRequest().getCreatePageRequest().getPageSize());

        //Chain of command - post create driver
        ContinueOrBreakChain<Optional<QueryResourceInfo>> postCreateDriver = getExecutionChain().postCreatePage(request, pageResourceInfo,cursorResourceInfo,queryResourceInfo);
        if(postCreateDriver.isBreakChain()) {
            return postCreateDriver.getReturnOnBreak();
        }

        //populate page resource
        cursorResourceInfo.get().setPageResourceInfos(Collections.singletonList(pageResourceInfo.get()));

        //Chain of command- pre get data
        ContinueOrBreakChain<Optional<QueryResourceInfo>> preGetData = getExecutionChain().preGetData(request,pageResourceInfo,cursorResourceInfo,queryResourceInfo);
        if(preGetData.isBreakChain())
            return preGetData.getReturnOnBreak();
        //get data
        Optional<Object> pageDataResponse = pageDriver.getData(queryResourceInfo.get().getResourceId(),
                cursorResourceInfo.get().getResourceId(),
                pageResourceInfo.get().getResourceId());
        //Chain of command - post get data
        ContinueOrBreakChain<Optional<QueryResourceInfo>> postGetData = getExecutionChain().postGetData(request,pageDataResponse,pageResourceInfo,cursorResourceInfo,queryResourceInfo);
        if(postGetData.isBreakChain())
            return postGetData.getReturnOnBreak();


        //populate data on page
        pageResourceInfo.get().setData(pageDataResponse.get());
        //finish - chain
        return Optional.of(
                new QueryResourceInfo(
                        queryResourceInfo.get().getType(),
                        queryResourceInfo.get().getResourceUrl(),
                        queryResourceInfo.get().getResourceId(),
                        cursorResourceInfo.get().getPageStoreUrl(),
                        cursorResourceInfo.get()
                ));
    }

    protected PlanWithCost<Plan, PlanDetailedCost> planWithCost(QueryMetadata metadata, AsgQuery query) {
        PlanWithCost<Plan, PlanDetailedCost> planWithCost = PlanWithCost.EMPTY_PLAN;

        //calculate execution plan - only when explicitly asked and type is not parameterized - cant count of evaluate "named" parameters
        if (metadata.isSearchPlan() && metadata.getType().equals(concrete)) {
            planWithCost = this.planSearcher.search(query);

            if (planWithCost == null) {
                throw new IllegalStateException("No valid plan was found for query " + (AsgQueryDescriptor.toString(query)));
            }
        }
        return planWithCost;
    }

    protected QueryResource createResource(CreateQueryRequest request, Query query, AsgQuery asgQuery, QueryMetadata metadata) {
        PlanWithCost<Plan, PlanDetailedCost> planWithCost = planWithCost(metadata, asgQuery);
        return new QueryResource(request, query, asgQuery, metadata, planWithCost, null);
    }

    protected abstract AsgQuery transform(T query);

    protected QueryPipeExecutionChain getExecutionChain() {
        return new QueryPipeExecutionChain();
    }

    public static class QueryPipeExecutionChain {
        public ContinueOrBreakChain<Optional<QueryResourceInfo>> preChainStart(CreateQueryRequestMetadata request, Optional<QueryResourceInfo> queryResourceInfo) {
            //validate resources exist & on no sufficient parameters
            if (!queryResourceInfo.isPresent() || queryResourceInfo.get().getError() != null) {
                if (queryResourceInfo.get().getError() != null) {
                    return BREAK(Optional.of(new QueryResourceInfo().error(queryResourceInfo.get().getError())));
                }
                return BREAK(Optional.of(new QueryResourceInfo().error(
                        new FuseError(Query.class.getSimpleName(), "Failed creating cursor resource from given request: \n" + request.toString()))));
            }

            return CONTINUE();
        }

        protected ContinueOrBreakChain<Optional<QueryResourceInfo>> preCreateCursor(CreateQueryRequestMetadata request, Optional<QueryResourceInfo> queryResourceInfo) {
            if (request.getCreateCursorRequest() == null) {
                return BREAK(queryResourceInfo);
            }

            return CONTINUE();
        }

        protected ContinueOrBreakChain<Optional<QueryResourceInfo>> postCreateCursor(CreateQueryRequestMetadata request, Optional<CursorResourceInfo> cursorResourceInfo, Optional<QueryResourceInfo> queryResourceInfo) {
            if (!cursorResourceInfo.isPresent()) {
                return BREAK(Optional.of(new QueryResourceInfo().error(
                        new FuseError(Query.class.getSimpleName(), "Failed creating cursor resource from given request: \n" + request.toString()))));
            }
            return CONTINUE();
        }

        protected ContinueOrBreakChain<Optional<QueryResourceInfo>> preCreatePage(CreateQueryRequestMetadata request, Optional<CursorResourceInfo> cursorResourceInfo, Optional<QueryResourceInfo> queryResourceInfo) {
            if (request.getCreateCursorRequest().getCreatePageRequest() == null) {
                return BREAK(Optional.of(new QueryResourceInfo(
                        queryResourceInfo.get().getType(),
                        queryResourceInfo.get().getResourceUrl(),
                        queryResourceInfo.get().getResourceId(),
                        cursorResourceInfo.get().getPageStoreUrl(),
                        cursorResourceInfo.get())));
            }

            return CONTINUE();
        }

        protected ContinueOrBreakChain<Optional<QueryResourceInfo>> postCreatePage(CreateQueryRequestMetadata request, Optional<PageResourceInfo> pageResourceInfo, Optional<CursorResourceInfo> cursorResourceInfo, Optional<QueryResourceInfo> queryResourceInfo) {
            if (!pageResourceInfo.isPresent()) {
                return BREAK(Optional.of(
                        new QueryResourceInfo(
                                queryResourceInfo.get().getType(),
                                queryResourceInfo.get().getResourceUrl(),
                                queryResourceInfo.get().getResourceId(),
                                cursorResourceInfo.get().getPageStoreUrl(),
                                cursorResourceInfo.get()
                        ).error(
                                new FuseError(Query.class.getSimpleName(),
                                        "Failed creating page resource from given request: \n" + request.toString()))));
            }
            return CONTINUE();
        }

        protected ContinueOrBreakChain<Optional<QueryResourceInfo>> preGetData(CreateQueryRequestMetadata request, Optional<PageResourceInfo> pageResourceInfo, Optional<CursorResourceInfo> cursorResourceInfo, Optional<QueryResourceInfo> queryResourceInfo) {
            return CONTINUE();
        }

        protected ContinueOrBreakChain<Optional<QueryResourceInfo>> postGetData(CreateQueryRequestMetadata request,Optional<Object> pageDataResponse, Optional<PageResourceInfo> pageResourceInfo, Optional<CursorResourceInfo> cursorResourceInfo, Optional<QueryResourceInfo> queryResourceInfo) {
            if (!pageDataResponse.isPresent()) {
                return BREAK(Optional.of(
                        new QueryResourceInfo(
                                queryResourceInfo.get().getType(),
                                queryResourceInfo.get().getResourceUrl(),
                                queryResourceInfo.get().getResourceId(),
                                cursorResourceInfo.get().getPageStoreUrl(),
                                cursorResourceInfo.get()
                        ).error(new FuseError(Query.class.getSimpleName(),
                                "Failed fetching page data from given request: \n" + request.toString()))));
            }

            return CONTINUE();
        }

    }

    public static class ContinueOrBreakChain<T> {
        private boolean breakChain;
        private T returnOnBreak;

        public ContinueOrBreakChain(boolean breakChain, T returnOnBreak) {
            this.breakChain = breakChain;
            this.returnOnBreak = returnOnBreak;
        }

        public boolean isBreakChain() {
            return breakChain;
        }

        public T getReturnOnBreak() {
            return returnOnBreak;
        }

        public static <T> ContinueOrBreakChain<T> BREAK(T returnOnBreak) {
            return new ContinueOrBreakChain<>(true,returnOnBreak);
        }

        public static <T> ContinueOrBreakChain<T> CONTINUE( ) {
            return new ContinueOrBreakChain<>(false,null);
        }
    }
}
