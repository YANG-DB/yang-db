package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.EBase;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import javaslang.control.Option;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class AsgPredicateRoutingStrategy<T extends EBase> implements AsgStrategy {

    public AsgPredicateRoutingStrategy(List<Tuple2<Predicate<T>, AsgElementStrategy<T>>> predicates, Function<AsgQuery, List<AsgEBase<T>>> elementSelector) {
        this.predicateWithStrategy = predicates;
        this.elementSelector = elementSelector;
    }

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        elementSelector.apply(query)
                .forEach(ePropGroupAsgEBase -> {
                    Option<AsgElementStrategy<T>> strategy = Stream.ofAll(predicateWithStrategy).find(p -> p._1()
                            .test(ePropGroupAsgEBase.geteBase()))
                            .map(Tuple2::_2);
                    //only when a strategy match - do it
                    if (!strategy.isEmpty())
                        strategy.get().apply(query, ePropGroupAsgEBase, context);
                });
    }

    private List<Tuple2<Predicate<T>, AsgElementStrategy<T>>> predicateWithStrategy;
    private Function<AsgQuery, List<AsgEBase<T>>> elementSelector;

}
