package com.kayhut.fuse.asg.strategy;

import com.google.inject.Inject;
import com.kayhut.fuse.asg.strategy.constraintTransformation.AsgConstraintIterableTransformationStrategy;
import com.kayhut.fuse.asg.strategy.constraintTransformation.AsgConstraintTypeTransformationStrategy;
import com.kayhut.fuse.asg.strategy.propertiesGrouping.AsgEntityPropertiesGroupingStrategy;
import com.kayhut.fuse.asg.strategy.propertiesGrouping.AsgHQuantifierPropertiesGroupingStrategy;
import com.kayhut.fuse.asg.strategy.propertiesGrouping.AsgQuant1PropertiesGroupingStrategy;
import com.kayhut.fuse.asg.strategy.propertiesGrouping.AsgRelPropertiesGroupingStrategy;
import com.kayhut.fuse.asg.strategy.selection.AsgDefaultSelectionStrategy;
import com.kayhut.fuse.asg.strategy.type.AsgUntypedInferTypeLeftSideRelationStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;

import java.util.Arrays;

/**
 * Created by Roman on 5/8/2017.
 */
public class M1AsgStrategyRegistrar implements AsgStrategyRegistrar {
    //region Constructors
    @Inject
    public M1AsgStrategyRegistrar(OntologyProvider ontologyProvider) {
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgStrategy> register() {
        return Arrays.asList(
                new AsgUntypedInferTypeLeftSideRelationStrategy(),
                new AsgEntityPropertiesGroupingStrategy(),
                new AsgHQuantifierPropertiesGroupingStrategy(),
                new AsgQuant1PropertiesGroupingStrategy(),
                new AsgRelPropertiesGroupingStrategy(),
                new AsgConstraintTypeTransformationStrategy(),
                new AsgConstraintIterableTransformationStrategy(),
                new AsgDefaultSelectionStrategy(this.ontologyProvider)
        );
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    //endregion
}
