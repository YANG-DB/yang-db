package com.yangdb.fuse.asg.translator.sparql.strategies;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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

import com.yangdb.fuse.asg.translator.sparql.strategies.expressions.ExpressionStrategies;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.List;


/**
 * Main entry point for the AST transformations
 */
public class RootTranslatorStrategy implements SparqlTranslatorStrategy{
    private final List<SparqlElementTranslatorStrategy> translatorStrategies;
    private final List<ExpressionStrategies> whereExpressionStrategies;

    public RootTranslatorStrategy(List<SparqlElementTranslatorStrategy> translatorStrategies, List<ExpressionStrategies> whereExpressionStrategies) {
        this.translatorStrategies = translatorStrategies;
        this.whereExpressionStrategies = whereExpressionStrategies;
    }

    @Override
    public void apply(AsgQuery query, SparqlStrategyContext context) {
        ParsedQuery statement = context.getStatement();
        Dataset dataset = statement.getDataset();
        //root parsed query entry level
        TupleExpr expr = statement.getTupleExpr();
        //structure query elements
        translatorStrategies.forEach(strategy->strategy.apply(expr,query,context));
    }


}
