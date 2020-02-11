package com.yangdb.fuse.core.driver;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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
import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.dispatcher.driver.CursorDriverBase;
import com.yangdb.fuse.dispatcher.driver.PageDriver;
import com.yangdb.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.yangdb.fuse.dispatcher.gta.TranslationContext;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.dispatcher.resource.CursorResource;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.executor.CompositeTraversalCursorContext;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.executor.ontology.UniGraphProvider;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import com.yangdb.fuse.model.transport.cursor.CreateInnerQueryCursorRequest;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.List;
import java.util.Optional;

import static com.yangdb.fuse.model.asgQuery.AsgCompositeQuery.hasInnerQuery;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class StandardCursorDriver extends CursorDriverBase {
    //region Constructors
    @Inject
    public StandardCursorDriver(
            ResourceStore resourceStore,
            PageDriver pageDriver,
            OntologyProvider ontologyProvider,
            PlanTraversalTranslator planTraversalTranslator,
            CursorFactory cursorFactory,
            UniGraphProvider uniGraphProvider,
            AppUrlSupplier urlSupplier) {
        super(resourceStore, urlSupplier);
        this.pageDriver = pageDriver;
        this.ontologyProvider = ontologyProvider;
        this.planTraversalTranslator = planTraversalTranslator;
        this.cursorFactory = cursorFactory;
        this.uniGraphProvider = uniGraphProvider;
    }
    //endregion

    //region CursorDriverBase Implementation
    @Override
    protected CursorResource createResource(QueryResource queryResource, String cursorId, CreateCursorRequest cursorRequest) {
        PlanWithCost<Plan, PlanDetailedCost> executionPlan = queryResource.getExecutionPlan();
        Ontology ontology = this.ontologyProvider.get(queryResource.getQuery().getOnt()).get();

        GraphTraversal<?, ?> traversal = createTraversal(executionPlan, ontology);

        //traversal.asAdmin().getSideEffects().register("profiler", Profiler.Impl::new, null);

        //todo in case of composite cursor -> add depended cursors for query
        //if query has inner queries -> create new CreateInnerQueryCursorRequest(cursorRequest)
        TraversalCursorContext context = createContext(queryResource, cursorRequest, ontology, traversal);
        Cursor cursor = this.cursorFactory.createCursor(context);

        return new CursorResource(cursorId, cursor, cursorRequest);
    }

    protected TraversalCursorContext createContext(QueryResource queryResource, CreateCursorRequest cursorRequest, Ontology ontology, GraphTraversal<?, ?> traversal) {
        TraversalCursorContext context = new TraversalCursorContext(
                ontologyProvider,
                ontology,
                queryResource,
                cursorRequest,
                traversal.path());
        if (hasInnerQuery(queryResource.getAsgQuery())) {
            List<QueryResource> queryResources = Stream.ofAll(queryResource.getInnerQueryResources()).toJavaList();
            //first level (hierarchy) inner queries
            return new CompositeTraversalCursorContext(
                    new TraversalCursorContext(
                            ontologyProvider,
                            ontology,
                            queryResource,
                            new CreateInnerQueryCursorRequest(cursorRequest),
                            traversal.path()), queryResources);
        }
        return context;
    }

    protected GraphTraversal<?, ?> createTraversal(PlanWithCost<Plan, PlanDetailedCost> plan, Ontology ontology) {
        try {
            return this.planTraversalTranslator.translate(
                    plan,
                    new TranslationContext(
                            new Ontology.Accessor(ontology),
                            uniGraphProvider.getGraph(ontology).traversal()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Optional<GraphTraversal> traversal(PlanWithCost plan, String ontology) {
        return Optional.of(createTraversal(plan, this.ontologyProvider.get(ontology).get()));
    }

    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private PlanTraversalTranslator planTraversalTranslator;
    private CursorFactory cursorFactory;
    private UniGraphProvider uniGraphProvider;

    //endregion
}
