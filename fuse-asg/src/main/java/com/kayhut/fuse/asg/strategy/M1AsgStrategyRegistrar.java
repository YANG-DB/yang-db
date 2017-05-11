package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.asg.strategy.ConstraintTransformation.AsgConstraintTransformationStrategy;
import com.kayhut.fuse.asg.strategy.PropertiesGrouping.AsgEntityPropertiesGroupingStrategy;
import com.kayhut.fuse.asg.strategy.PropertiesGrouping.AsgHQuantifierPropertiesGroupingStrategy;
import com.kayhut.fuse.asg.strategy.PropertiesGrouping.AsgQuant1PropertiesGroupingStrategy;
import com.kayhut.fuse.asg.strategy.PropertiesGrouping.AsgRelPropertiesGroupingStrategy;

import java.util.Arrays;

/**
 * Created by Roman on 5/8/2017.
 */
public class M1AsgStrategyRegistrar implements AsgStrategyRegistrar {
    //region AsgStrategyRegistrar Implementation
    @Override
    public Iterable<AsgStrategy> register() {
        return Arrays.asList(
                new AsgEntityPropertiesGroupingStrategy(),
                new AsgHQuantifierPropertiesGroupingStrategy(),
                new AsgQuant1PropertiesGroupingStrategy(),
                new AsgRelPropertiesGroupingStrategy(),
                new AsgConstraintTransformationStrategy()
        );
    }
    //endregion
}
