package com.yangdb.fuse.asg.translator.cypher;

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
import com.yangdb.fuse.asg.translator.AsgTranslator;
import com.yangdb.fuse.asg.translator.cypher.strategies.CypherStrategyContext;
import com.yangdb.fuse.asg.translator.cypher.strategies.CypherTranslatorStrategy;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.QueryInfo;
import org.opencypher.v9_0.ast.Statement;
import org.opencypher.v9_0.parser.CypherParser;
import scala.Option;

public class CypherTranslator implements AsgTranslator<QueryInfo<String>,AsgQuery> {

    @Inject
    public CypherTranslator(CypherAsgStrategyRegistrar strategies) {
        this.strategies = strategies.register();
    }
    //endregion


    @Override
    public AsgQuery translate(QueryInfo<String> source) {

        final AsgQuery query = AsgQuery.Builder.start("cypher_", source.getOntology()).build();

        //translate cypher asci into cypher AST
        final Statement statement = new CypherParser().parse(source.getQuery(),Option.empty());
        final CypherStrategyContext context = new CypherStrategyContext(statement,query.getStart());

        //apply strategies
        strategies.iterator().forEachRemaining(cypherTranslatorStrategy -> cypherTranslatorStrategy.apply(query, context));
        return query;
    }

    private Iterable<CypherTranslatorStrategy> strategies;
}
