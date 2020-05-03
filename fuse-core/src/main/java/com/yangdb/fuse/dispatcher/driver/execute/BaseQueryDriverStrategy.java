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
import com.yangdb.fuse.dispatcher.epb.PlanSearcher;
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
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryMetadata;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.NamedParameter;
import com.yangdb.fuse.model.query.properties.constraint.ParameterizedConstraint;
import com.yangdb.fuse.model.query.properties.constraint.QueryNamedParameter;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.results.AssignmentUtils;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;
import com.yangdb.fuse.model.transport.CreateJsonQueryRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequestMetadata;
import com.yangdb.fuse.model.validation.ValidationResult;
import javaslang.collection.Stream;
import javaslang.control.Option;

import java.util.*;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.asgQuery.AsgCompositeQuery.hasInnerQuery;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.QueryType.concrete;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.QueryType.parameterized;
import static java.util.Collections.EMPTY_LIST;

public class BaseQueryDriverStrategy implements QueryDriverStrategy<CreateJsonQueryRequest>{
    private final CursorDriver cursorDriver;
    private QueryTransformer<AsgQuery, AsgQuery> queryRewriter;
    private final JsonQueryTransformerFactory transformerFactory;
    private final QueryValidator<AsgQuery> queryValidator;
    private final ResourceStore resourceStore;
    private PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher;
    private final AppUrlSupplier urlSupplier;

    @Inject
    public BaseQueryDriverStrategy(
            CursorDriver cursorDriver,
            QueryTransformer<AsgQuery, AsgQuery> queryRewriter,
            JsonQueryTransformerFactory transformerFactory,
            QueryValidator<AsgQuery> queryValidator,
            ResourceStore resourceStore,
            PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher,
            AppUrlSupplier urlSupplier) {

        this.cursorDriver = cursorDriver;
        this.queryRewriter = queryRewriter;
        this.transformerFactory = transformerFactory;
        this.queryValidator = queryValidator;
        this.resourceStore = resourceStore;
        this.planSearcher = planSearcher;
        this.urlSupplier = urlSupplier;
    }

    @Override
    public boolean test(CreateJsonQueryRequest request) {
        return request.getType().equals(CreateQueryRequestMetadata.TYPE_CYPHER);
    }

    @Override
    public QueryResourceInfo execute(CreateJsonQueryRequest request,QueryMetadata metadata) {
        AsgQuery asgQuery = transform(request);
        asgQuery.setName(metadata.getName());
        asgQuery.setOnt(request.getOntology());
        //rewrite query
        asgQuery = rewrite(asgQuery);


        ValidationResult validationResult = validateAsgQuery(asgQuery);
        if (!validationResult.valid()) {
            return new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),
                            validationResult.getValidator() + ":" + Arrays.toString(Stream.ofAll(validationResult.errors()).toJavaArray(String.class))));
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

        return new QueryResourceInfo(
                metadata.getType(),
                urlSupplier.resourceUrl(metadata.getId()),
                metadata.getId(),
                urlSupplier.cursorStoreUrl(metadata.getId()))
                .withInnerQueryResources(getQueryResourceInfos(innerQuery));
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
    protected AsgQuery transform(CreateJsonQueryRequest query) {
        return this.transformerFactory.transform(query.getType()).transform(query);
    }

    protected AsgQuery rewrite(AsgQuery asgQuery) {
        return this.queryRewriter.transform(asgQuery);
    }

    protected ValidationResult validateAsgQuery(AsgQuery query) {
        return this.queryValidator.validate(query);
    }


    private List<QueryResourceInfo> getQueryResourceInfos(List<QueryResource> innerQuery) {
        return innerQuery.stream().map(qr -> new QueryResourceInfo(
                qr.getQueryMetadata().getType(),
                urlSupplier.resourceUrl(qr.getQueryMetadata().getId()),
                qr.getQueryMetadata().getId(),
                urlSupplier.cursorStoreUrl(qr.getQueryMetadata().getId())))
                .collect(Collectors.toList());
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


}
