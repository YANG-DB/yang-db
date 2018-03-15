package com.kayhut.fuse.assembly.knowlegde;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.epb.PlanExtensionStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.epb.plan.extenders.*;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import javaslang.collection.Stream;

/**
 * Created by roman.margolis on 15/03/2018.
 */
public class KnowledgeM1DfsRedundantPlanExtensionStrategy extends CompositePlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    @Inject
    public KnowledgeM1DfsRedundantPlanExtensionStrategy(
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory) {
        super();

        this.innerExtenders = Stream.<PlanExtensionStrategy<Plan, AsgQuery>>of(
                new ChainPlanExtensionStrategy<>(
                        new CompositePlanExtensionStrategy<>(
                                new KnowledgeInitialPlanGeneratorExtensionStrategy(),
                                new StepAdjacentDfsStrategy(),
                                new OptionalOpExtensionStrategy(this)
                        ),
                        new OptionalInitialExtensionStrategy(),
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
