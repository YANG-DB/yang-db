package com.yangdb.fuse.ext.driver;

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
import com.yangdb.fuse.assembly.knowledge.KnowledgeGraphHierarchyCursorRequest;
import com.yangdb.fuse.assembly.knowledge.KnowledgeLogicalGraphCursorRequest;
import com.yangdb.fuse.assembly.knowledge.asg.AsgClauseTransformer;
import com.yangdb.fuse.assembly.knowledge.parser.JsonQueryTranslator;
import com.yangdb.fuse.assembly.knowledge.parser.model.BusinessTypesProvider;
import com.yangdb.fuse.core.driver.StandardQueryDriver;
import com.yangdb.fuse.dispatcher.driver.CursorDriver;
import com.yangdb.fuse.dispatcher.driver.PageDriver;
import com.yangdb.fuse.dispatcher.epb.PlanSearcher;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.dispatcher.validation.QueryValidator;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryMetadata;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.transport.CreateJsonQueryRequest;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest;

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

    public Optional<QueryResourceInfo> createClause(CreateJsonQueryRequest request) {
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

    public Optional<Object> runClause(String clause, String ontology) {
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

    protected CreateJsonQueryRequest createJsonQueryRequest(String cypher, String ontology, String id) {
        return new CreateJsonQueryRequest(id, id, cypher, ontology, new KnowledgeLogicalGraphCursorRequest(new CreatePageRequest()));
    }

    protected CreateQueryRequest createQueryRequest(Query query, String id) {
        return new CreateQueryRequest(id, id, query, new KnowledgeLogicalGraphCursorRequest(new CreatePageRequest()));
    }


    //endregion
    private QueryTransformer<String, Query> transformer;
    private BusinessTypesProvider typesProvider;
}
