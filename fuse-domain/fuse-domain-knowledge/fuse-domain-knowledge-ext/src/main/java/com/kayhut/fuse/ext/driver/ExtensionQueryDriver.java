package com.kayhut.fuse.ext.driver;

/*-
 * #%L
 * fuse-dv-core
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
import com.kayhut.fuse.assembly.knowledge.asg.AsgClauseTransformer;
import com.kayhut.fuse.assembly.knowledge.parser.JsonQueryTranslator;
import com.kayhut.fuse.assembly.knowledge.parser.model.BusinessTypesProvider;
import com.kayhut.fuse.core.driver.StandardQueryDriver;
import com.kayhut.fuse.dispatcher.driver.CursorDriver;
import com.kayhut.fuse.dispatcher.driver.PageDriver;
import com.kayhut.fuse.dispatcher.driver.QueryDriverBase;
import com.kayhut.fuse.dispatcher.epb.PlanSearcher;
import com.kayhut.fuse.dispatcher.query.QueryTransformer;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.dispatcher.validation.QueryValidator;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import com.kayhut.fuse.model.resourceInfo.FuseError;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.transport.CreateJsonQueryRequest;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;

import java.util.Optional;
import java.util.UUID;

public class ExtensionQueryDriver extends StandardQueryDriver {

    public static final String TYPE_CLAUSE = "clause";

    //region Constructors
    @Inject
    public ExtensionQueryDriver(
            CursorDriver cursorDriver,
            PageDriver pageDriver,
            QueryTransformer<Query, AsgQuery> queryTransformer,
            QueryValidator<AsgQuery> queryValidator,
            QueryTransformer<AsgQuery, AsgQuery> queryRewriter,
            QueryTransformer<String, AsgQuery> jsonQueryTransformer,
            PlanSearcher<Plan, PlanDetailedCost, AsgQuery> planSearcher,
            ResourceStore resourceStore,
            BusinessTypesProvider typesProvider,
            AppUrlSupplier urlSupplier) {
        super(cursorDriver, pageDriver, queryTransformer, queryValidator, queryRewriter,jsonQueryTransformer,planSearcher,resourceStore, urlSupplier);
        this.typesProvider = typesProvider;
        this.transformer = new AsgClauseTransformer(new JsonQueryTranslator(),typesProvider);
    }
    //endregion

    //region Implementation

    @Override
    public Optional<QueryResourceInfo> create(CreateJsonQueryRequest request) {
        try {
            QueryMetadata metadata = getQueryMetadata(request);
            Optional<QueryResourceInfo> queryResourceInfo = Optional.empty();
            if(request.getQueryType().equals(CreateJsonQueryRequest.TYPE_CYPHER)) {
                //support cypher type
                queryResourceInfo = this.create(request, metadata, request.getQuery());
            } else if(request.getQueryType().equals(TYPE_CLAUSE)){
                //support clause type
                Query query = transformer.transform(request.getQuery());
                queryResourceInfo = this.create(new CreateQueryRequest(request.getId(),request.getName(),query,request.getCreateCursorRequest()));
            }
            return getQueryResourceInfo(request, queryResourceInfo);
        } catch (Exception err) {
            return Optional.of(new QueryResourceInfo().error(
                    new FuseError(Query.class.getSimpleName(),
                            err.getMessage())));

        }
    }

    @Override
    public Optional<Object> run(String clause, String ontology) {
        String id = UUID.randomUUID().toString();
        try {
            CreateJsonQueryRequest queryRequest = new CreateJsonQueryRequest(id, id, TYPE_CLAUSE, clause, ontology,new CreateGraphCursorRequest(new CreatePageRequest()));
            Optional<QueryResourceInfo> resourceInfo = create(queryRequest);

            if(!resourceInfo.isPresent())
                return Optional.empty();

            if(resourceInfo.get().getError()!=null)
                return Optional.of(resourceInfo.get().getError());

            return Optional.of(resourceInfo.get());
        } finally {
            //remove stateless query
            delete(id);
        }
    }

    //endregion
    private QueryTransformer<String, Query> transformer;
    private BusinessTypesProvider typesProvider;
}
