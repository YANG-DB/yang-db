package com.kayhut.fuse.assembly.knowledge;

import com.google.inject.Inject;
import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.asg.strategy.AsgStrategyRegistrar;
import com.kayhut.fuse.asg.strategy.RuleBoostProvider;
import com.kayhut.fuse.asg.strategy.constraint.*;
import com.kayhut.fuse.asg.strategy.propertyGrouping.*;
import com.kayhut.fuse.asg.strategy.schema.ExactConstraintTransformationAsgStrategy;
import com.kayhut.fuse.asg.strategy.schema.LikeAnyConstraintTransformationAsgStrategy;
import com.kayhut.fuse.asg.strategy.selection.DefaultSelectionAsgStrategy;
import com.kayhut.fuse.asg.strategy.type.UntypedInferTypeLeftSideRelationAsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;

import java.util.Arrays;

public class KnowledgeM2AsgStrategyRegistrar implements AsgStrategyRegistrar {
    //region Constructors
    @Inject
    public KnowledgeM2AsgStrategyRegistrar(OntologyProvider ontologyProvider,
                                           GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
        this.ruleBoostProvider = new KnowledgeRuleBoostProvider();
    }

    public KnowledgeM2AsgStrategyRegistrar(OntologyProvider ontologyProvider,
                                           GraphElementSchemaProviderFactory schemaProviderFactory,
                                           RuleBoostProvider ruleBoostProvider) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
        this.ruleBoostProvider = ruleBoostProvider;
    }
    //endregion

    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgStrategy> register() {
        return Arrays.asList(
                new UntypedInferTypeLeftSideRelationAsgStrategy(),
                new EntityPropertiesGroupingAsgStrategy(),
                new HQuantifierPropertiesGroupingAsgStrategy(),
                new Quant1PropertiesGroupingAsgStrategy(),
                new RelPropertiesGroupingAsgStrategy(),
                new ConstraintTypeTransformationAsgStrategy(),
                new ConstraintIterableTransformationAsgStrategy(),
                new RedundantLikeConstraintAsgStrategy(),
                new RedundantLikeAnyConstraintAsgStrategy(),
                new LikeToEqTransformationAsgStrategy(),
                new ExactConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                //remove like Constraint Strategy since we have it inside the combiner
//                new LikeConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new LikeAnyConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                //knowledge ranking asg strategies
                new KnowledgeLikeCombinerStrategy(ruleBoostProvider, ontologyProvider, schemaProviderFactory),
                new RankingPropertiesPropagationAsgStrategy(),
                new RedundantInSetConstraintAsgStrategy(),
                new RedundantPropGroupAsgStrategy(),
                new DefaultSelectionAsgStrategy(this.ontologyProvider)

        );
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    private RuleBoostProvider ruleBoostProvider;
    //endregion
}
