package com.kayhut.fuse.epb.plan.extenders;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;

/**
 * Created by Roman on 22/05/2017.
 */
public class M1DfsRedundantPlanExtensionStrategy extends CompositePlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    @Inject
    public M1DfsRedundantPlanExtensionStrategy(OntologyProvider ontologyProvider, GraphElementSchemaProvider schemaProvider) {
        super(
                new ChainPlanExtensionStrategy<>(
                        new CompositePlanExtensionStrategy<>(
                                new InitialPlanGeneratorExtensionStrategy(),
                                new StepAdjacentDfsStrategy()
                        ),
                        new PushDownSplitFilterPlanExtensionStrategy(ontologyProvider, schemaProvider)
                )
        );
    }
    //endregion
}
