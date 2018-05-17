package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class AsgPredicateRoutingStrategy<T extends EBase> implements AsgStrategy {

    public AsgPredicateRoutingStrategy(Predicate<T> predicate, Function<AsgQuery, List<AsgEBase<T>>> elementSelector, AsgElementStrategy<T> trueStrategy, AsgElementStrategy<T> falseStrategy) {
        this.predicate = predicate;
        this.elementSelector = elementSelector;
        this.trueStrategy = trueStrategy;
        this.falseStrategy = falseStrategy;
    }

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        elementSelector.apply(query).forEach(ePropGroupAsgEBase -> {
            if(predicate.test(ePropGroupAsgEBase.geteBase())){
                trueStrategy.apply(query,  ePropGroupAsgEBase, context);
            }
            else {
                falseStrategy.apply(query,  ePropGroupAsgEBase, context);
            }
        });
    }

    private Predicate<T> predicate;
    private Function<AsgQuery, List<AsgEBase<T>>> elementSelector;
    private AsgElementStrategy<T> trueStrategy;
    private AsgElementStrategy<T> falseStrategy;

}
