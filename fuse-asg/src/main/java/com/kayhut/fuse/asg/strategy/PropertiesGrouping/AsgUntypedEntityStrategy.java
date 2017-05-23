package com.kayhut.fuse.asg.strategy.PropertiesGrouping;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.asg.strategy.AsgStrategyContext;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.entity.EUntyped;
import javaslang.collection.Stream;

/**
 * try to infer type for empty list of vTypes in an UnTyped entity
 */
public class AsgUntypedEntityStrategy implements AsgStrategy {
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Stream.ofAll(AsgQueryUtil.<EUntyped>elements(query, EUntyped.class))
                .filter(asgEBase -> asgEBase.geteBase().getvTypes().isEmpty())
                .forEach(asgEBase -> {
//                    AsgQueryUtil.<EUntyped>elements(query, EUntyped.class)
                });

    }
    //endregion
}
