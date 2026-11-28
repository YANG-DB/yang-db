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

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.dispatcher.driver.CursorDriverBase;
import com.yangdb.fuse.dispatcher.driver.PageDriver;
import com.yangdb.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.yangdb.fuse.dispatcher.gta.TranslationContext;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.dispatcher.provision.CursorRuntimeProvision;
import com.yangdb.fuse.dispatcher.profile.QueryProfileInfo;
import com.yangdb.fuse.dispatcher.resource.CursorResource;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.dispatcher.resource.store.ResourceStore;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.executor.CompositeTraversalCursorContext;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.UniGraphProvider;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import com.yangdb.fuse.model.transport.cursor.CreateInnerQueryCursorRequest;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.opensearch.client.Client;
import org.unipop.process.Profiler;
import org.unipop.process.Profiler.Noop;

import com.google.inject.name.Named;
import com.yangdb.fuse.dispatcher.provision.PointInTimes;
import com.yangdb.fuse.dispatcher.utils.PlanUtil;
import com.yangdb.fuse.model.execution.plan.PlanOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.opensearch.action.search.CreatePitAction;
import org.opensearch.action.search.CreatePitRequest;
import org.opensearch.action.search.CreatePitResponse;
import org.opensearch.action.search.DeletePitAction;
import org.opensearch.action.search.DeletePitRequest;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.core.action.ActionListener;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import java.util.Optional;

import static com.yangdb.fuse.model.asgQuery.AsgCompositeQuery.hasInnerQuery;
import static org.unipop.process.Profiler.PROFILER;

/**
 * Created by lior.perry on 20/02/2017.
 */
public class StandardCursorDriver extends CursorDriverBase {
    private final Client client;
    private final GraphElementSchemaProviderFactory schemaProvider;

    //region Constructors
    @Inject
    public StandardCursorDriver(
            Client client,
            ResourceStore resourceStore,
            PageDriver pageDriver,
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory,
            PlanTraversalTranslator planTraversalTranslator,
            CursorFactory cursorFactory,
            UniGraphProvider uniGraphProvider,
            AppUrlSupplier urlSupplier,
            MetricRegistry registry) {
        super(registry, resourceStore, urlSupplier);
        this.client = client;
        this.schemaProvider = schemaProviderFactory;
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
        //get the ontology name from the asg query since a transformation between ontologies might have occurred - see AsgMappingStrategy
        Ontology ontology = this.ontologyProvider.get(queryResource.getAsgQuery().getOnt())
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No target Ontology field found ", "No target Ontology field found for " + queryResource.getAsgQuery().getOnt())));

        GraphTraversal<?, ?> traversal = createTraversal(executionPlan, ontology);

        //execution context
        String prefix = String.format("%s.%s", queryResource.getQueryMetadata().getId(), cursorId);
        traversal.asAdmin().getSideEffects().register(CONTEXT, () -> prefix, null);

        //a consistent cursor reads one Point in Time for all its searches and pages (ROADMAP 7.8)
        if (isConsistent(cursorRequest)) {
            openPointInTime(prefix, executionPlan.getPlan(), ontology, queryResource.getQueryMetadata().getTtl());
        }

        //default no operation profiler - no memory footprint or activity
        traversal.asAdmin().getSideEffects().register(PROFILER, () -> Noop.instance, null);

//      activate profiling configuration - override noop default profiler with a real one - large memory footprint - caution
        if (cursorRequest.isProfile()) {
            traversal.asAdmin().getSideEffects().register(PROFILER, Profiler.Impl::new, null);
        }

        //todo in case of composite cursor -> add depended cursors for query
        //if query has inner queries -> create new CreateInnerQueryCursorRequest(cursorRequest)
        TraversalCursorContext context = createContext(queryResource, cursorRequest, cursorId, ontology, traversal);
        Cursor cursor = this.cursorFactory.createCursor(context);
        Profiler profiler = traversal.asAdmin().getSideEffects().get(PROFILER);

        return new CursorResource(cursorId, cursor, new QueryProfileInfo.QueryProfileInfoImpl(profiler.get()), cursorRequest);
    }

    /** the request's choice, else opensearch.paging.mode (scroll unless set to pit) */
    boolean isConsistent(CreateCursorRequest cursorRequest) {
        return cursorRequest.getConsistent() != null
                ? cursorRequest.getConsistent()
                : PAGING_MODE_PIT.equalsIgnoreCase(this.pagingMode);
    }

    private void openPointInTime(String cursorContext, Plan plan, Ontology ontology, long queryTtl) {
        String[] indices = planIndices(plan, this.schemaProvider.get(ontology)).toArray(new String[0]);
        if (indices.length == 0) {
            return; // nothing to search: the cursor's searches have no indices either
        }
        // the PIT lives while the cursor may still be read: the query's ttl between pages (each search extends it)
        long keepAlive = queryTtl > 0 ? queryTtl : DEFAULT_PIT_KEEP_ALIVE_MS;
        CreatePitRequest request = new CreatePitRequest(TimeValue.timeValueMillis(keepAlive), false, indices);
        // the schema may name indices that don't exist (a type with no data yet, e.g. Dragons' profession): a scroll
        // search skips them, so the PIT does too
        request.indicesOptions(org.opensearch.action.support.IndicesOptions.LENIENT_EXPAND_OPEN);
        CreatePitResponse response = this.client.execute(CreatePitAction.INSTANCE, request).actionGet();
        String pitId = response.getId();
        PointInTimes.register(cursorContext, new PointInTimes.Handle(pitId, keepAlive, () ->
                this.client.execute(DeletePitAction.INSTANCE, new DeletePitRequest(pitId),
                        ActionListener.wrap(r -> { }, e -> { /* expires with its keep-alive anyway */ }))));
    }

    /**
     * The indices a plan reads: those of its entity and relationship types in the physical schema (all vertex types for an
     * untyped entity). The PIT is opened on these only: each of their shards holds one PIT context on the cluster.
     */
    static Set<String> planIndices(Plan plan, GraphElementSchemaProvider schema) {
        Set<String> labelsV = new LinkedHashSet<>();
        Set<String> labelsE = new LinkedHashSet<>();
        Plan flat = PlanUtil.flat(plan);
        for (PlanOp op : flat.getOps()) {
            if (op instanceof EntityOp) {
                EEntityBase e = ((EntityOp) op).getAsgEbase().geteBase();
                if (e instanceof ETyped) {
                    labelsV.add(((ETyped) e).geteType());
                } else if (e instanceof EUntyped && !((EUntyped) e).getvTypes().isEmpty()) {
                    labelsV.addAll(((EUntyped) e).getvTypes());
                } else {
                    schema.getVertexLabels().forEach(labelsV::add);
                }
            } else if (op instanceof RelationOp) {
                labelsE.add(((RelationOp) op).getAsgEbase().geteBase().getrType());
            }
        }
        Set<String> indices = new LinkedHashSet<>();
        labelsV.forEach(l -> schema.getVertexSchemas(l).forEach(v -> v.getIndexPartitions()
                .ifPresent(p -> p.getIndices().forEach(indices::add))));
        labelsE.forEach(l -> schema.getEdgeSchemas(l).forEach(e -> e.getIndexPartitions()
                .ifPresent(p -> p.getIndices().forEach(indices::add))));
        return indices;
    }

    @com.google.inject.Inject(optional = true)
    public void setPagingMode(@Named(PAGING_MODE) String pagingMode) {
        this.pagingMode = pagingMode;
    }

    protected TraversalCursorContext createContext(QueryResource queryResource, CreateCursorRequest cursorRequest, String cursorId, Ontology ontology, GraphTraversal<?, ?> traversal) {
        String prefix = String.format("%s.%s", queryResource.getQueryMetadata().getId(), cursorId);
        TraversalCursorContext context = new TraversalCursorContext(
                client,
                schemaProvider.get(ontology),
                ontologyProvider,
                ontology,
                queryResource,
                cursorRequest,
                new CursorRuntimeProvision.MetricRegistryCursorRuntimeProvision(prefix, registry),
                traversal.path());
        if (hasInnerQuery(queryResource.getAsgQuery())) {
            List<QueryResource> queryResources = Stream.ofAll(queryResource.getInnerQueryResources()).toJavaList();
            //first level (hierarchy) inner queries
            return new CompositeTraversalCursorContext(
                    new TraversalCursorContext(
                            client,
                            schemaProvider.get(ontology),
                            ontologyProvider,
                            ontology,
                            queryResource,
                            new CreateInnerQueryCursorRequest(cursorRequest),
                            new CursorRuntimeProvision.MetricRegistryCursorRuntimeProvision(prefix, registry),
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
        return Optional.of(createTraversal(plan, this.ontologyProvider.get(ontology)
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No target Ontology field found ", "No target Ontology found for " + ontology)))));
    }

    //endregion

    //region Fields
    public static final String PAGING_MODE = "opensearch.paging.mode";
    public static final String PAGING_MODE_PIT = "pit";
    static final long DEFAULT_PIT_KEEP_ALIVE_MS = 5 * 60 * 1000;
    private String pagingMode = "scroll";
    private OntologyProvider ontologyProvider;
    private PlanTraversalTranslator planTraversalTranslator;
    private CursorFactory cursorFactory;
    private UniGraphProvider uniGraphProvider;


    //endregion
}
