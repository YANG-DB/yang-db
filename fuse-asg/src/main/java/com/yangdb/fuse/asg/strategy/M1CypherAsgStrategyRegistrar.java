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



import com.google.inject.Inject;
import com.yangdb.fuse.asg.translator.cypher.strategies.*;
import com.yangdb.fuse.asg.translator.cypher.strategies.expressions.*;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class M1CypherAsgStrategyRegistrar implements CypherAsgStrategyRegistrar {
    private final OntologyProvider ontologyProvider;
    private final OntologyTransformerProvider transformerProvider;

    //region Constructors
    @Inject
    public M1CypherAsgStrategyRegistrar(OntologyProvider ontologyProvider,OntologyTransformerProvider transformerProvider) {
        this.ontologyProvider = ontologyProvider;
        this.transformerProvider = transformerProvider;
    }

    //endregion

    @Override
    public Iterable<CypherTranslatorStrategy> register() {
        //translators
        translatorStrategies = Arrays.asList(
                new NodePatternCypherTranslatorStrategy(new EqualityExpression()),
                new StepPatternCypherTranslatorStrategy(
                        new NodePatternCypherTranslatorStrategy(new EqualityExpression()),
                        new EqualityExpression()
                ),
                new LogicalGraphTransformerTranslatorStrategy(ontologyProvider,transformerProvider)
        );

        //expressions
        whereExpressionStrategies = new ArrayList<>();
        whereExpressionStrategies.add(new OrExpression(whereExpressionStrategies));
        whereExpressionStrategies.add(new AndExpression(whereExpressionStrategies));
        whereExpressionStrategies.add(new HasLabelExpression());
        whereExpressionStrategies.add(new HasRelationLabelExpression());
        whereExpressionStrategies.add(new InequalityExpression());
        whereExpressionStrategies.add(new EqualityExpression());
        whereExpressionStrategies.add(new NotEqualExpression());
        whereExpressionStrategies.add(new StartsWithExpression());
        whereExpressionStrategies.add(new EndsWithExpression());
        whereExpressionStrategies.add(new InExpression());
        whereExpressionStrategies.add(new ContainsExpression());
        whereExpressionStrategies.add(new LikeExpression());

        whereClause = new WhereClauseNodeCypherTranslator(whereExpressionStrategies);
        match = new MatchCypherTranslatorStrategy(translatorStrategies, whereClause);

        return Collections.singleton(match);
    }

    private List<CypherElementTranslatorStrategy> translatorStrategies;
    private List<ExpressionStrategies> whereExpressionStrategies;

    public MatchCypherTranslatorStrategy match;
    private WhereClauseNodeCypherTranslator whereClause;
}
