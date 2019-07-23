package com.yangdb.fuse.asg.translator.cypher.strategies.expressions;

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

import com.yangdb.fuse.asg.translator.cypher.strategies.CypherElementTranslatorStrategy;
import com.yangdb.fuse.asg.translator.cypher.strategies.CypherStrategyContext;
import com.yangdb.fuse.asg.translator.cypher.strategies.CypherUtils;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import org.opencypher.v9_0.ast.Where;

import java.util.Optional;

public class WhereClauseNodeCypherTranslator implements CypherElementTranslatorStrategy<Where> {

    public WhereClauseNodeCypherTranslator(Iterable<ExpressionStrategies> strategies) {
        this.strategies = strategies;
    }

    public void apply(Where where, AsgQuery query, CypherStrategyContext context) {
        final com.bpodgursky.jbool_expressions.Expression c = CypherUtils.reWrite(where.expression());
        strategies.forEach(s->{
            if(s.isApply(c)) s.apply(Optional.empty(), c, query, context);
        });
    }

    private Iterable<ExpressionStrategies> strategies;
}
