package com.yangdb.fuse.asg.translator.cypher.strategies.expressions;

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

import com.bpodgursky.jbool_expressions.Expression;
import com.yangdb.fuse.asg.translator.cypher.strategies.CypherElementTranslatorStrategy;
import com.yangdb.fuse.asg.translator.cypher.strategies.CypherStrategyContext;
import com.yangdb.fuse.asg.translator.cypher.strategies.CypherUtils;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import org.opencypher.v9_0.ast.Return;

import java.util.Optional;

import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class ReturnClauseNodeCypherTranslator implements CypherElementTranslatorStrategy<Return> {

    public ReturnClauseNodeCypherTranslator(Iterable<ExpressionStrategies> strategies) {
        this.strategies = strategies;
    }

    public void apply(Return aReturn, AsgQuery query, CypherStrategyContext context) {
        asJavaCollectionConverter(aReturn.returnItems().items())
                .asJavaCollection().forEach(
                item -> {
                    Expression c = CypherUtils.reWrite(item.expression());
                    strategies.forEach(s -> {
                        if (s.isApply(c)) s.apply(Optional.empty(), c, query, context);
                    });
                }
        );
    }

    private Iterable<ExpressionStrategies> strategies;
}
