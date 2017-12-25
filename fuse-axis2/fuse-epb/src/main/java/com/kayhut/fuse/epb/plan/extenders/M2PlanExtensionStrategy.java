package com.kayhut.fuse.epb.plan.extenders;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.epb.plan.extenders.*;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;

import java.util.Optional;

/**
 * Created by Roman on 21/05/2017.
 */
public class M2PlanExtensionStrategy extends CompositePlanExtensionStrategy<Plan, AsgQuery> {
    //region Constructors
    @Inject
    public M2PlanExtensionStrategy(
            OntologyProvider ontologyProvider,
            GraphElementSchemaProviderFactory schemaProviderFactory) {
        super(
                new ChainPlanExtensionStrategy<>(
                        new CompositePlanExtensionStrategy<>(
                                new InitialPlanGeneratorExtensionStrategy(),
                                new JoinSeedExtensionStrategy(new InitialPlanGeneratorExtensionStrategy()),
                                new JoinOngoingExtensionStrategy(
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
                                        ))),
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
                        new RedundantFilterPlanExtensionStrategy(
                                ontologyProvider,
                                schemaProviderFactory)
                )
        );
    }
    //endregion

    @Override
    public Iterable<Plan> extendPlan(Optional<Plan> plan, AsgQuery query) {
        return super.extendPlan(plan, query);
    }
}
