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

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.kayhut.fuse.asg.translator.cypher.strategies.CypherStrategyContext;
import com.kayhut.fuse.asg.translator.cypher.strategies.CypherUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.asg.translator.cypher.strategies.CypherUtils.reverse;

public class AndExpression implements ExpressionStrategies {

    public AndExpression(Iterable<ExpressionStrategies> strategies) {
        this.strategies = strategies;
    }

    @Override
    public void apply(Optional<Expression> parent, Expression expression, AsgQuery query, CypherStrategyContext context) {
        //filter only AND expressions
        if ((expression instanceof com.bpodgursky.jbool_expressions.And)) {
            //todo parent is empty - create a 'all'-quant as query start
            if(!parent.isPresent()) {
                CypherUtils.quant(query.getStart().getNext().isEmpty() ? query.getStart() : query.getStart().getNext().get(0), Optional.of(expression), query, context);
            }

            And and = (And) expression;
            reverse(((List<Expression>) and.getChildren()))
                    .forEach(c -> {
                        context.scope(query.getStart());
                        strategies.forEach(s -> s.apply(Optional.of(and), c, query, context));
                    });
        }
    }

    private Iterable<ExpressionStrategies> strategies;

}
