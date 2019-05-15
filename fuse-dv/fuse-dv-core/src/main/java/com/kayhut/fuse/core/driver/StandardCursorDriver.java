package com.kayhut.fuse.core.driver;

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
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.driver.CursorDriverBase;
import com.kayhut.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.dispatcher.resource.store.ResourceStore;
import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.executor.ontology.UniGraphProvider;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Optional;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class StandardCursorDriver extends CursorDriverBase {
    //region Constructors
    @Inject
    public StandardCursorDriver(
            ResourceStore resourceStore,
            OntologyProvider ontologyProvider,
            PlanTraversalTranslator planTraversalTranslator,
            CursorFactory cursorFactory,
            UniGraphProvider uniGraphProvider,
            AppUrlSupplier urlSupplier) {
        super(resourceStore, urlSupplier);
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

        Cursor cursor = this.cursorFactory.createCursor(
                new TraversalCursorContext(
                        ontology,
                        queryResource,
                        cursorRequest,
                        traversal.path()));

        return new CursorResource(cursorId, cursor, cursorRequest);
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
        return Optional.of(createTraversal(plan,this.ontologyProvider.get(ontology).get()));
    }

    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private PlanTraversalTranslator planTraversalTranslator;
    private CursorFactory cursorFactory;
    private UniGraphProvider uniGraphProvider;

    //endregion
}
