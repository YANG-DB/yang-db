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

import com.bpodgursky.jbool_expressions.Expression;
import com.kayhut.fuse.asg.translator.cypher.strategies.CypherStrategyContext;
import com.kayhut.fuse.asg.translator.cypher.strategies.CypherUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import org.opencypher.v9_0.expressions.*;

import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.asg.translator.cypher.strategies.CypherUtils.reverse;

public class OrExpression implements ExpressionStrategies {

    public OrExpression(Iterable<ExpressionStrategies> strategies) {
        this.strategies = strategies;
    }

    @Override
    public void apply(Optional<com.bpodgursky.jbool_expressions.Expression> parent, com.bpodgursky.jbool_expressions.Expression expression, AsgQuery query, CypherStrategyContext context) {
        //filter only AND expressions
        if ((expression instanceof com.bpodgursky.jbool_expressions.Or)) {
            //todo parent is empty - create a 'all'-quant as query start
            if(!parent.isPresent()) {
                CypherUtils.quant(query.getStart(), Optional.of(expression), query, context);
            }

            com.bpodgursky.jbool_expressions.Or or = (com.bpodgursky.jbool_expressions.Or) expression;
            reverse(((List<Expression>) or.getChildren()))
                    .forEach(c -> strategies.forEach(s -> s.apply(Optional.of(or), c, query, context)));
        }
    }

    private Iterable<ExpressionStrategies> strategies;

}
