package com.yangdb.fuse.asg.strategy;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.query.EBase;
import javaslang.collection.Stream;

import java.util.function.Function;
import java.util.function.Predicate;

public class AsgPredicateRoutingStrategy<T extends EBase> implements AsgStrategy {
    //region Constructors
    public AsgPredicateRoutingStrategy(Iterable<Routing<T>> routings, Function<AsgQuery, Iterable<AsgEBase<T>>> elementSelector) {
        this.routings = routings;
        this.elementSelector = elementSelector;
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Stream.ofAll(this.elementSelector.apply(query))
                .forEach(selectedElement -> {
                    Stream.ofAll(routings)
                            .filter(routing -> routing.predicate.test(selectedElement))
                            .toJavaOptional()
                            .ifPresent(routing -> routing.getStrategy().apply(query, selectedElement, context));

                });
    }
    //endregion

    //region Fields
    private Iterable<Routing<T>> routings;
    private Function<AsgQuery, Iterable<AsgEBase<T>>> elementSelector;
    //endregion

    public static class Routing<T extends EBase> {
        //region Constructors
        public Routing(Predicate<AsgEBase<T>> predicate, AsgElementStrategy<T> strategy) {
            this.predicate = predicate;
            this.strategy = strategy;
        }
        //endregion

        //region Properties
        public Predicate<AsgEBase<T>> getPredicate() {
            return predicate;
        }

        public AsgElementStrategy<T> getStrategy() {
            return strategy;
        }
        //endregion

        //region Fields
        private Predicate<AsgEBase<T>> predicate;
        private AsgElementStrategy<T> strategy;
        //endregion
    }
}
