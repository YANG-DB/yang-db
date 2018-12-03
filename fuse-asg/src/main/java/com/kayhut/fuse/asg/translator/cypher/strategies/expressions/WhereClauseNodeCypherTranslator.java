package com.kayhut.fuse.asg.translator.cypher.strategies.expressions;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2018 The Fuse Graph Database Project
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

import com.kayhut.fuse.asg.translator.cypher.strategies.CypherElementTranslatorStrategy;
import com.kayhut.fuse.asg.translator.cypher.strategies.CypherStrategyContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import org.opencypher.v9_0.ast.Where;
import org.opencypher.v9_0.expressions.Expression;

import java.util.Optional;

public class WhereClauseNodeCypherTranslator implements CypherElementTranslatorStrategy<Where> {

    public WhereClauseNodeCypherTranslator(Iterable<ExpressionStrategies> strategies) {
        this.strategies = strategies;
    }

    public void apply(Where where, AsgQuery query, CypherStrategyContext context) {
        Expression expression = where.expression();
        strategies.forEach(s->s.apply(Optional.empty(), expression, query, context));
    }

    private Iterable<ExpressionStrategies> strategies;
}
