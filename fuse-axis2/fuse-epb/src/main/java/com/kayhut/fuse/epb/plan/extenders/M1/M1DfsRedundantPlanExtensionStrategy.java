package com.kayhut.fuse.epb.plan.extenders.M1;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.*;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import javaslang.collection.Stream;

/**
 * Created by Roman on 22/05/2017.
 */
public class M1DfsRedundantPlanExtensionStrategy extends CompositePlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    @Inject
    public M1DfsRedundantPlanExtensionStrategy(
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory) {
        super();

        this.innerExtenders = Stream.<PlanExtensionStrategy<Plan, AsgQuery>>of(
                        new ChainPlanExtensionStrategy<>(
                                new CompositePlanExtensionStrategy<>(
                                        new InitialPlanGeneratorExtensionStrategy(),
                                        new StepAdjacentDfsStrategy(),
                                        new OptionalBranchExtensionStrategy(this)
                                ),
                                new RedundantFilterPlanExtensionStrategy(
                                        ontologyProvider,
                                        schemaProviderFactory),
                                new RedundantSelectionFilterPlanExtensionStrategy(
                                        ontologyProvider,
                                        schemaProviderFactory)
                        )
        ).toJavaList();
    }
    //endregion
}
