package com.kayhut.fuse.epb.plan.extenders;

import com.codahale.metrics.Slf4jReporter;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.LoggerAnnotation;
import com.kayhut.fuse.executor.ontology.GraphLayoutProviderFactory;
import com.kayhut.fuse.executor.ontology.PhysicalIndexProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;

/**
 * Created by Roman on 21/05/2017.
 */
public class M1PlanExtensionStrategy extends CompositePlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    @Inject
    public M1PlanExtensionStrategy(
            OntologyProvider ontologyProvider,
            PhysicalIndexProviderFactory physicalIndexProviderFactory,
            GraphLayoutProviderFactory graphLayoutProviderFactory) {
        super(
                new ChainPlanExtensionStrategy<>(
                        new CompositePlanExtensionStrategy<>(
                                new StepAncestorAdjacentStrategy(),
                                new StepDescendantsAdjacentStrategy(),
                                new ChainPlanExtensionStrategy<>(
                                        new GotoExtensionStrategy(),
                                        new CompositePlanExtensionStrategy<>(
                                                new StepAncestorAdjacentStrategy(),
                                                new StepDescendantsAdjacentStrategy()
                                        )
                                )
                        ),
                        new PushDownSplitFilterPlanExtensionStrategy(
                                ontologyProvider,
                                physicalIndexProviderFactory,
                                graphLayoutProviderFactory)
                )
        );
    }
    //endregion

    @Override
    @LoggerAnnotation(name = "extendPlan", options = LoggerAnnotation.Options.full, logLevel = Slf4jReporter.LoggingLevel.DEBUG)
    public Iterable<Plan> extendPlan(Plan plan, AsgQuery query) {
        return super.extendPlan(plan, query);
    }
}
