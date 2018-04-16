package com.kayhut.fuse.asg.strategy;

import com.google.inject.Inject;
import com.kayhut.fuse.asg.strategy.constraint.ConstraintIterableTransformationAsgStrategy;
import com.kayhut.fuse.asg.strategy.constraint.ConstraintTypeTransformationAsgStrategy;
import com.kayhut.fuse.asg.strategy.constraint.RedundantLikeAnyConstraintAsgStrategy;
import com.kayhut.fuse.asg.strategy.constraint.RedundantLikeConstraintAsgStrategy;
import com.kayhut.fuse.asg.strategy.propertyGrouping.EntityPropertiesGroupingAsgStrategy;
import com.kayhut.fuse.asg.strategy.propertyGrouping.HQuantifierPropertiesGroupingAsgStrategy;
import com.kayhut.fuse.asg.strategy.propertyGrouping.Quant1PropertiesGroupingAsgStrategy;
import com.kayhut.fuse.asg.strategy.propertyGrouping.RelPropertiesGroupingAsgStrategy;
import com.kayhut.fuse.asg.strategy.selection.DefaultSelectionAsgStrategy;
import com.kayhut.fuse.asg.strategy.type.UntypedInferTypeLeftSideRelationAsgStrategy;
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
                new UntypedInferTypeLeftSideRelationAsgStrategy(),
                new EntityPropertiesGroupingAsgStrategy(),
                new HQuantifierPropertiesGroupingAsgStrategy(),
                new Quant1PropertiesGroupingAsgStrategy(),
                new RelPropertiesGroupingAsgStrategy(),
                new RedundantLikeConstraintAsgStrategy(),
                new ConstraintTypeTransformationAsgStrategy(),
                new ConstraintIterableTransformationAsgStrategy(),
                new RedundantLikeConstraintAsgStrategy(),
                new RedundantLikeAnyConstraintAsgStrategy(),
                new DefaultSelectionAsgStrategy(this.ontologyProvider)
        );
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    //endregion
}
