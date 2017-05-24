package com.kayhut.fuse.epb.plan.extenders;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.executor.uniGraphProvider.GraphLayoutProviderFactory;
import com.kayhut.fuse.executor.uniGraphProvider.PhysicalIndexProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;

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
                                new InitialPlanGeneratorExtensionStrategy(),
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
}
