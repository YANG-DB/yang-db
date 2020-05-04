package com.yangdb.fuse.dispatcher.driver;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
import com.yangdb.fuse.dispatcher.query.JsonQueryTransformerFactory;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.dispatcher.resource.CursorResource;
import com.yangdb.fuse.dispatcher.resource.PageResource;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.dispatcher.validation.QueryValidator;
import com.yangdb.fuse.model.asgQuery.AsgCompositeQuery;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.planTree.PlanNode;
import com.yangdb.fuse.model.query.ParameterizedQuery;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryMetadata;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.NamedParameter;
import com.yangdb.fuse.model.query.properties.constraint.ParameterizedConstraint;
import com.yangdb.fuse.model.query.properties.constraint.QueryNamedParameter;
import com.yangdb.fuse.model.resourceInfo.*;
import com.yangdb.fuse.model.results.AssignmentUtils;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.transport.*;
import com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.*;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import com.yangdb.fuse.model.transport.cursor.LogicalGraphCursorRequest;
import com.yangdb.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;
import javaslang.control.Option;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.*;
import java.util.stream.Collectors;

import static com.yangdb.fuse.dispatcher.cursor.CursorFactory.request;
import static com.yangdb.fuse.model.Utils.getOrCreateId;
import static com.yangdb.fuse.model.asgQuery.AsgCompositeQuery.hasInnerQuery;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.QueryType.parameterized;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.*;
import static java.util.Collections.EMPTY_LIST;

/**
 * Created by Roman on 12/15/2017.
 */
public abstract class QueryDriverBase implements QueryDriver {
    //region Constructors
    @Inject
    public QueryDriverBase(
            CursorDriver cursorDriver,
            PageDriver pageDriver,
            QueryTransformer<Query, AsgQuery> queryTransformer,
            JsonQueryTransformerFactory transformerFactory,
            QueryValidator<AsgQuery> queryValidator,
            ResourceStore resourceStore,
            AppUrlSupplier urlSupplier) {
        this.cursorDriver = cursorDriver;
        this.pageDriver = pageDriver;
        this.queryTransformer = queryTransformer;
        this.transformerFactory = transformerFactory;
        this.queryValidator = queryValidator;
        this.resourceStore = resourceStore;
        this.urlSupplier = urlSupplier;
    }
    //endregion

    //region QueryDriver Implementation
    @Override
    @Deprecated()
    public Optional<QueryResourceInfo> createAndFetch(CreateQueryRequest request) {
        return create(request);
    }

    @Override
    public Optional<Object> run(Query query, int pageSize, String cursorType) {
        String id = UUID.randomUUID().toString();
        try {
            CreateQueryRequest queryRequest = new CreateQueryRequest(id, id, query,
                    request(query.getOnt(),cursorType,new CreatePageRequest(pageSize)));
            Optional<QueryResourceInfo> resourceInfo = create(queryRequest);
            if (!resourceInfo.isPresent())
                return Optional.empty();

            if (resourceInfo.get().getError() != null)
                return Optional.of(resourceInfo.get().getError());

            return Optional.of(resourceInfo.get());
        } catch (Throwable e) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(), "failed building the cursor request " + cursorType)));
        } finally {
            //remove stateless query
//            delete(id);
        }

    }

    @Override
    public Optional<Object> runCypher(String cypher, String ontology, int pageSize, String cursorType) {
        String id = UUID.randomUUID().toString();
        try {
            CreateJsonQueryRequest queryRequest = new CreateJsonQueryRequest(id, id, TYPE_CYPHER, cypher, ontology,
                    request(ontology,cursorType,new CreatePageRequest(pageSize)));
            Optional<QueryResourceInfo> resourceInfo = create(queryRequest);
            if (!resourceInfo.isPresent())
                return Optional.empty();

            if (resourceInfo.get().getError() != null)
                return Optional.of(resourceInfo.get().getError());

            return Optional.of(resourceInfo.get());
        } catch (Throwable e) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(), "failed building the cursor request " + cursorType)));

        } finally {
            //remove stateless query
//            delete(id);
        }
    }

    @Override
    public Optional<Object> runGremlin(String query, String ontology, int pageSize, String cursorType) {
        String id = UUID.randomUUID().toString();
        try {
            CreateJsonQueryRequest queryRequest = new CreateJsonQueryRequest(id, id, TYPE_GREMLIN, query, ontology,
                    new LogicalGraphCursorRequest(ontology,new CreatePageRequest()));
            Optional<QueryResourceInfo> resourceInfo = create(queryRequest);
            if (!resourceInfo.isPresent())
                return Optional.empty();

            if (resourceInfo.get().getError() != null)
                return Optional.of(resourceInfo.get().getError());

            return Optional.of(resourceInfo.get());
        } finally {
            //remove stateless query
//            delete(id);
        }
    }

    @Override
    public Optional<Object> runGraphQL(String graphQL, String ontology) {
        String id = UUID.randomUUID().toString();
        try {
            CreateJsonQueryRequest queryRequest = new CreateJsonQueryRequest(id, id, TYPE_GRAPH_QL, graphQL, ontology,
                    new LogicalGraphCursorRequest(ontology,new CreatePageRequest()));
            Optional<QueryResourceInfo> resourceInfo = create(queryRequest);
            if (!resourceInfo.isPresent())
                return Optional.empty();

            if (resourceInfo.get().getError() != null)
                return Optional.of(resourceInfo.get().getError());

            return Optional.of(resourceInfo.get());
        } finally {
            //remove stateless query
//            delete(id);
        }
    }

    @Override
    public Optional<Object> runGraphQL(String graphQL, String ontology, int pageSize, String cursorType) {
        String id = UUID.randomUUID().toString();
        try {
            CreateJsonQueryRequest queryRequest = new CreateJsonQueryRequest(id, id, TYPE_GRAPH_QL, graphQL, ontology,
                    request(ontology,cursorType,new CreatePageRequest(pageSize)));
            Optional<QueryResourceInfo> resourceInfo = create(queryRequest);
            if (!resourceInfo.isPresent())
                return Optional.empty();

            if (resourceInfo.get().getError() != null)
                return Optional.of(resourceInfo.get().getError());

            return Optional.of(resourceInfo.get());
        } catch (Throwable e) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(), "failed building the cursor request " + cursorType)));
        } finally {
            //remove stateless query
//            delete(id);
        }
    }

    @Override
    public Optional<Object> runCypher(String cypher, String ontology) {
        String id = UUID.randomUUID().toString();
        try {
            CreateJsonQueryRequest queryRequest = new CreateJsonQueryRequest(id, id, TYPE_CYPHER, cypher, ontology,
                    new LogicalGraphCursorRequest(ontology,new CreatePageRequest()));
            Optional<QueryResourceInfo> resourceInfo = create(queryRequest);
            if (!resourceInfo.isPresent())
                return Optional.empty();

            if (resourceInfo.get().getError() != null)
                return Optional.of(resourceInfo.get().getError());

            return Optional.of(resourceInfo.get());
        } finally {
            //remove stateless query
//            delete(id);
        }
    }


    @Override
    public Optional<Object> getNextPageData(String queryId, Optional<String> cursorId, int pageSize, boolean deleteCurrentPage) {
        try {
            if (!resourceStore.getQueryResource(queryId).isPresent())
                return Optional.of(new QueryResourceInfo().error(
                        new FuseError(Query.class.getSimpleName(),
                                "Query with id[" + queryId + "] not found in store")));

            QueryResource queryResource = resourceStore.getQueryResource(queryId).get();
            final String cursorID = cursorId.orElse(queryResource.getCurrentCursorId());
            final Optional<PageResourceInfo> info = pageDriver.create(queryId, cursorID, pageSize);
            if (!info.isPresent())
                return Optional.of(new QueryResourceInfo().error(
                        new FuseError(Query.class.getSimpleName(), "failed fetching next page for query " + queryId)));

            final PageResourceInfo pageResourceInfo = info.get();

            if (deleteCurrentPage) {
                final String priorPageId = resourceStore.getCursorResource(queryId, cursorID).get().getPriorPageId();
                pageDriver.delete(queryId, cursorID, priorPageId);
            }
            return pageDriver.getData(queryId, cursorID, pageResourceInfo.getResourceId());
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),
                            err.getMessage())));
        }
    }

    /**
     * internal api
     *
     * @param request
     * @param metadata
     * @param query
     * @return
     */
    private Optional<QueryResourceInfo> create(CreateQueryRequest request, QueryMetadata metadata, Query query) {
        try {
            AsgQuery asgQuery = transform(query);
            asgQuery = rewrite(asgQuery);

            ValidationResult validationResult = this.queryValidator.validate(asgQuery);

            if (!validationResult.valid()) {
                return Optional.of(new QueryResourceInfo().error(
                        new FuseError(Query.class.getSimpleName(),
                                validationResult.getValidator() + ":"
                                        + Arrays.toString(Stream.ofAll(validationResult.errors()).toJavaArray(String.class)))));
            }

            //create inner query
            final List<QueryResource> innerQuery = compositeQuery(request, metadata, asgQuery);
            //outer most query resource
            this.resourceStore.addQueryResource(createResource(request, query, asgQuery, metadata)
                    .withInnerQueryResources(innerQuery));

            return Optional.of(new QueryResourceInfo(
                    metadata.getType(),
                    urlSupplier.resourceUrl(metadata.getId()),
                    metadata.getId(),
                    urlSupplier.cursorStoreUrl(metadata.getId()))
                    .withInnerQueryResources(getQueryResourceInfos(innerQuery)));
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),
                            err.toString())));
        }
    }

    private List<QueryResourceInfo> getQueryResourceInfos(List<QueryResource> innerQuery) {
        return innerQuery.stream().map(qr -> new QueryResourceInfo(
                qr.getQueryMetadata().getType(),
                urlSupplier.resourceUrl(qr.getQueryMetadata().getId()),
                qr.getQueryMetadata().getId(),
                urlSupplier.cursorStoreUrl(qr.getQueryMetadata().getId())))
                .collect(Collectors.toList());
    }

    /**
     * add inner query to repository with related parent query name
     *
     * @param request
     * @param metadata
     * @param outer
     */
    private List<QueryResource> compositeQuery(CreateQueryRequestMetadata request, QueryMetadata metadata, AsgQuery outer) {
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
    private QueryResource getQueryResource(CreateQueryRequestMetadata request, QueryMetadata metadata, AsgQuery inner) {
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


    /**
     * internal api
     *
     * @param request
     * @param metadata
     * @return
     */
    protected Optional<QueryResourceInfo> create(CreateJsonQueryRequest request, QueryMetadata metadata) {
        try {
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

            Query build = Query.Builder.instance()
//                    .withOnt(request.getOntology())
                    .withOnt(asgQuery.getOnt())
                    .withName(request.getName())
                    .build();

            //create inner query
            final List<QueryResource> innerQuery = compositeQuery(request, metadata, asgQuery);

            //outer most query resource
            this.resourceStore.addQueryResource(createResource(
                    new CreateQueryRequest(request.getId(),
                            request.getName(),
                            build,
                            request.getPlanTraceOptions(),
                            request.getCreateCursorRequest())
                    , build
                    , asgQuery
                    , metadata)
                    .withInnerQueryResources(innerQuery));

            return Optional.of(new QueryResourceInfo(
                    metadata.getType(),
                    urlSupplier.resourceUrl(metadata.getId()),
                    metadata.getId(),
                    urlSupplier.cursorStoreUrl(metadata.getId()))
                    .withInnerQueryResources(getQueryResourceInfos(innerQuery)));
        } catch (FuseError.FuseErrorException err) {
            return Optional.of(new QueryResourceInfo().error(err.getError()));
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),err)));
        }
    }

    protected ValidationResult validateAsgQuery(AsgQuery query) {
        return this.queryValidator.validate(query);
    }

    public ValidationResult validateAndRewriteQuery(Query query) {
        AsgQuery asgQuery = transform(query);
        if (!validateAsgQuery(asgQuery).valid())
            return validateAsgQuery(asgQuery);
        return validateAsgQuery(rewrite(asgQuery));
    }

    protected AsgQuery transform(Query query) {
        return this.queryTransformer.transform(query);
    }

    protected AsgQuery transform(CreateJsonQueryRequest queryRequest) {
        return this.transformerFactory.transform(queryRequest.getType()).transform(queryRequest);
    }

    @Override
    public Optional<QueryResourceInfo> create(CreateJsonQueryRequest request) {
        try {
            QueryMetadata metadata = getQueryMetadata(request);
            Optional<QueryResourceInfo> queryResourceInfo = this.create(request, metadata);
            return getQueryResourceInfo(request, queryResourceInfo);
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(), err)));

        }
    }

    @Override
    public Optional<QueryResourceInfo> create(CreateQueryRequest request) {
        try {
            QueryMetadata metadata = getQueryMetadata(request);
            Optional<QueryResourceInfo> queryResourceInfo = this.create(request, metadata, request.getQuery());
            return getQueryResourceInfo(request, queryResourceInfo);
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),
                            err)));

        }

    }

    protected Optional<QueryResourceInfo> getQueryResourceInfo(CreateQueryRequestMetadata request, Optional<QueryResourceInfo> queryResourceInfo) {
        if (!queryResourceInfo.isPresent() || queryResourceInfo.get().getError() != null) {
            if (queryResourceInfo.get().getError() != null) {
                return Optional.of(new QueryResourceInfo().error(queryResourceInfo.get().getError()));
            }
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(), "Failed creating cursor resource from given request: \n" + request.toString())));
        }

        if (request.getCreateCursorRequest() == null) {
            return queryResourceInfo;
        }

        Optional<CursorResourceInfo> cursorResourceInfo = this.cursorDriver.create(queryResourceInfo.get().getResourceId(), request.getCreateCursorRequest());
        if (!cursorResourceInfo.isPresent()) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(), "Failed creating cursor resource from given request: \n" + request.toString())));
        }

        if (request.getCreateCursorRequest().getCreatePageRequest() == null) {
            return Optional.of(new QueryResourceInfo(
                    queryResourceInfo.get().getType(),
                    queryResourceInfo.get().getResourceUrl(),
                    queryResourceInfo.get().getResourceId(),
                    cursorResourceInfo.get().getPageStoreUrl(),
                    cursorResourceInfo.get()));
        }

        Optional<PageResourceInfo> pageResourceInfo = this.pageDriver.create(
                queryResourceInfo.get().getResourceId(),
                cursorResourceInfo.get().getResourceId(),
                request.getCreateCursorRequest().getCreatePageRequest().getPageSize());

        if (!pageResourceInfo.isPresent()) {
            return Optional.of(
                    new QueryResourceInfo(
                            queryResourceInfo.get().getType(),
                            queryResourceInfo.get().getResourceUrl(),
                            queryResourceInfo.get().getResourceId(),
                            cursorResourceInfo.get().getPageStoreUrl(),
                            cursorResourceInfo.get()
                    ).error(
                            new FuseError(Query.class.getSimpleName(),
                                    "Failed creating page resource from given request: \n" + request.toString())));
        }

        cursorResourceInfo.get().setPageResourceInfos(Collections.singletonList(pageResourceInfo.get()));

        Optional<Object> pageDataResponse = pageDriver.getData(queryResourceInfo.get().getResourceId(),
                cursorResourceInfo.get().getResourceId(),
                pageResourceInfo.get().getResourceId());

        if (!pageDataResponse.isPresent()) {
            return Optional.of(
                    new QueryResourceInfo(
                            queryResourceInfo.get().getType(),
                            queryResourceInfo.get().getResourceUrl(),
                            queryResourceInfo.get().getResourceId(),
                            cursorResourceInfo.get().getPageStoreUrl(),
                            cursorResourceInfo.get()
                    ).error(new FuseError(Query.class.getSimpleName(), "Failed fetching page data from given request: \n" + request.toString())));
        }
        //populate data on page
        pageResourceInfo.get().setData(pageDataResponse.get());

        //handle parameterized query -> will eventually call this getQueryResourceInfo() method with the real parameterized concrete values
        Optional<QueryResourceInfo> resourceInfo = parameterizedQuery(request, queryResourceInfo);
        if (resourceInfo.isPresent()) {
            String innerQueryResourceId = queryResourceInfo.get().getInnerUrlResourceInfos().get(0).getResourceId();
            Optional<QueryResourceInfo> info = getInfo(innerQueryResourceId);
            resourceInfo.get().withInnerQueryResources(Collections.singletonList(info.get()));
            return resourceInfo;
        }

        return Optional.of(
                new QueryResourceInfo(
                        queryResourceInfo.get().getType(),
                        queryResourceInfo.get().getResourceUrl(),
                        queryResourceInfo.get().getResourceId(),
                        cursorResourceInfo.get().getPageStoreUrl(),
                        cursorResourceInfo.get()
                ));
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
        if (queryResourceInfo.get().getType() == QueryType.parameterized) {
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

    protected QueryMetadata getQueryMetadata(CreateQueryRequestMetadata request) {
        String queryId = getOrCreateId(request.getId());
        return new QueryMetadata(request.getQueryType(), request.getStorageType(), queryId, request.getName(), request.isSearchPlan(), System.currentTimeMillis(), request.getTtl());
    }

    @Override
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

    @Override
    public Optional<StoreResourceInfo> getInfo() {
        Iterable<String> resourceUrls = Stream.ofAll(this.resourceStore.getQueryResources())
                .sortBy(queryResource -> queryResource.getQueryMetadata().getCreationTime())
                .map(queryResource -> queryResource.getQueryMetadata().getId())
                .map(this.urlSupplier::resourceUrl)
                .toJavaList();

        return Optional.of(new StoreResourceInfo(this.urlSupplier.queryStoreUrl(), null, resourceUrls));
    }

    @Override
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

    @Override
    public Optional<AsgQuery> getAsg(String queryId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(queryResource.get().getAsgQuery());
    }

    @Override
    public Optional<Query> getV1(String queryId) {
        Optional<QueryResource> queryResource = this.resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(queryResource.get().getQuery());
    }

    @Override
    public Optional<PlanWithCost<Plan, PlanDetailedCost>> explain(String queryId) {
        Optional<QueryResource> queryResource = resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(queryResource.get().getExecutionPlan());
    }

    @Override
    public Optional<PlanNode<Plan>> planVerbose(String queryId) {
        Optional<QueryResource> queryResource = resourceStore.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return Optional.empty();
        }

        return queryResource.get().getPlanNode();
    }

    @Override
    public Optional<Boolean> delete(String queryId) {
        Optional<QueryResource> resource = resourceStore.getQueryResource(queryId);
        if (!resource.isPresent())
            return Optional.of(Boolean.FALSE);
        //composite query delete
        resource.get().getInnerQueryResources().forEach(inner -> delete(inner.getQueryMetadata().getId()));
        return Optional.of(resourceStore.deleteQueryResource(queryId));
    }

    @Override
    public Optional<PlanWithCost<Plan, PlanDetailedCost>> plan(Query query) {
        AsgQuery asgQuery = transform(query);
        if (!validateAsgQuery(asgQuery).valid())
            return Optional.of(new PlanWithCost.ErrorPlanWithCost(
                    new FuseError("PlanValidationError", validateAsgQuery(asgQuery).toString())));

        AsgQuery rewrite = rewrite(asgQuery);
        if (!validateAsgQuery(rewrite).valid())
            return Optional.of(new PlanWithCost.ErrorPlanWithCost(
                    new FuseError("PlanValidationError", validateAsgQuery(rewrite).toString())));

        try {
            return Optional.of(planWithCost(QueryMetadata.random("plan", true), rewrite));
        } catch (Exception e) {
            return Optional.of(new PlanWithCost.ErrorPlanWithCost(new FuseError("NoValidPlanFound", e)));
        }
    }

    @Override
    public Optional<GraphTraversal> traversal(Query query) {
        final PlanWithCost<Plan, PlanDetailedCost> planWithCost = plan(query).get();
        return this.cursorDriver.traversal(planWithCost, query.getOnt());
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
    //endregion

    //region Protected Abstract Methods
    protected abstract QueryResource createResource(CreateQueryRequest request, Query query, AsgQuery asgQuery, QueryMetadata metadata);

    protected abstract PlanWithCost<Plan, PlanDetailedCost> planWithCost(QueryMetadata metadata, AsgQuery query);

    protected abstract AsgQuery rewrite(AsgQuery asgQuery);
    //endregion

    //region Fields
    private final CursorDriver cursorDriver;
    private final PageDriver pageDriver;
    private QueryTransformer<Query, AsgQuery> queryTransformer;
    private JsonQueryTransformerFactory transformerFactory;
    private QueryValidator<AsgQuery> queryValidator;
    private ResourceStore resourceStore;
    private final AppUrlSupplier urlSupplier;
    //endregion
}
