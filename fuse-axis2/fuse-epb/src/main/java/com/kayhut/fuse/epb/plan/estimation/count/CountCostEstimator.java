package com.kayhut.fuse.epb.plan.estimation.count;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.epb.CostEstimator;
import com.kayhut.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.kayhut.fuse.executor.ontology.UniGraphProvider;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.ontology.Ontology;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Collections;
import java.util.Optional;

/**
 * Created by Roman on 3/14/2018.
 */
public class CountCostEstimator implements CostEstimator<Plan, PlanDetailedCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> {
    //region Constructors
    @Inject
    public CountCostEstimator(
            OntologyProvider ontologyProvider,
            PlanTraversalTranslator planTraversalTranslator,
            UniGraphProvider uniGraphProvider) {

        this.ontologyProvider = ontologyProvider;
        this.planTraversalTranslator = planTraversalTranslator;
        this.uniGraphProvider = uniGraphProvider;
    }
    //endregion

    //region CostEstimator Implementation
    @Override
    public PlanWithCost<Plan, PlanDetailedCost> estimate(Plan plan, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery> estimationContext) {
        Ontology ontology = this.ontologyProvider.get(estimationContext.getQuery().getOnt()).get();
        GraphTraversal<?, ?> traversal = null;
        try {
            traversal = this.planTraversalTranslator.translate(
                    new PlanWithCost<>(plan, new PlanDetailedCost(new DoubleCost(0.0), Collections.emptyList())),
                    new TranslationContext(
                            new Ontology.Accessor(ontology),
                            uniGraphProvider.getGraph(ontology).traversal()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        long count = traversal.count().next();

        return new PlanWithCost<>(plan, new PlanDetailedCost(new DoubleCost(count), Collections.emptyList()));
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private PlanTraversalTranslator planTraversalTranslator;
    private UniGraphProvider uniGraphProvider;
    //endregion
}
