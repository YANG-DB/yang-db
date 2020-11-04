package com.yangdb.fuse.asg.translator.sparql;

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


import com.google.inject.Inject;
import com.yangdb.fuse.asg.strategy.SparqlAsgStrategyRegistrar;
import com.yangdb.fuse.asg.translator.sparql.strategies.*;
import com.yangdb.fuse.asg.translator.sparql.strategies.expressions.CompareExpressionStrategy;
import com.yangdb.fuse.asg.translator.sparql.strategies.expressions.ExpressionStrategies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Todo - Work in progress ...
 */
public class M1SparqlAsgStrategyRegistrar implements SparqlAsgStrategyRegistrar {

    //region Constructors
    @Inject
    public M1SparqlAsgStrategyRegistrar() {}

    //endregion

    @Override
    public Iterable<SparqlTranslatorStrategy> register() {
        //expressions
        whereExpressionStrategies.add(new CompareExpressionStrategy());

        //translators
        translatorStrategies.addAll(Arrays.asList(
                new ProjectionPatternTranslatorStrategy(
                        Arrays.asList(
                                new FilterPatternTranslatorStrategy(translatorStrategies, whereExpressionStrategies),
                                new ArbitraryPathTranslatorStrategy(translatorStrategies),
                                new JoinPatternTranslatorStrategy(translatorStrategies),
                                new UnionPatternTranslatorStrategy(translatorStrategies),
                                new NodePatternTranslatorStrategy())
                ),
                new FilterPatternTranslatorStrategy(translatorStrategies, whereExpressionStrategies),
                new ArbitraryPathTranslatorStrategy(translatorStrategies),
                new JoinPatternTranslatorStrategy(translatorStrategies),
                new UnionPatternTranslatorStrategy(translatorStrategies),
                new NodePatternTranslatorStrategy()
        ));

        return Collections.singleton(new RootTranslatorStrategy(translatorStrategies, whereExpressionStrategies));
    }

    private List<SparqlElementTranslatorStrategy> translatorStrategies = new ArrayList<>();
    private List<ExpressionStrategies> whereExpressionStrategies = new ArrayList<>();

}

