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
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import org.opencypher.v9_0.ast.Statement;
import org.opencypher.v9_0.parser.CypherParser;
import org.opencypher.v9_0.util.SyntaxException;
import scala.Option;

public class CypherTranslator implements AsgTranslator<QueryInfo<String>, AsgQuery> {

    @Inject
    public CypherTranslator(CypherAsgStrategyRegistrar strategies) {
        this.strategies = strategies.register();
    }
    //endregion


    @Override
    public AsgQuery translate(QueryInfo<String> source) {

        final AsgQuery query = AsgQuery.Builder.start("cypher_", source.getOntology()).build();

        //translate cypher asci into cypher AST
        final Statement statement = parse(source.getQuery());
        final CypherStrategyContext context = new CypherStrategyContext(statement, query.getStart());


        //apply strategies
        strategies.iterator().forEachRemaining(cypherTranslatorStrategy -> cypherTranslatorStrategy.apply(query, context));

        //collect projection fields
        query.setProjectedFields(AsgQueryUtil.groupByTags(query.getStart()));
        return query;
    }

    /**
     * Parses with the openCypher 9 front end. That grammar has no node-label union ({@code (n:A|B)}, Cypher 5 / GQL),
     * so such queries are rejected with an explicit unsupported-syntax error naming the supported alternatives
     * instead of the bare parser message. Every other syntax error is rethrown unchanged.
     */
    static Statement parse(String cypher) {
        try {
            return new CypherParser().parse(cypher, Option.empty());
        } catch (SyntaxException e) {
            if (isNodeLabelUnion(e)) {
                throw new FuseError.FuseErrorException("Unsupported Cypher syntax", e,
                        new FuseError("Unsupported Cypher syntax",
                                "Node label union such as (n:A|B) is not supported. Use WHERE n:A OR n:B, " +
                                        "or (n:A:B), which matches nodes of any of the listed labels. Parser said: " + e.getMessage()));
            }
            throw e;
        }
    }

    /** True when the parser stopped at a '|' whose innermost open bracket is a node pattern '(' (not a rel '['). */
    private static boolean isNodeLabelUnion(SyntaxException e) {
        final String query = e.query();
        if (query == null || e.pos().isEmpty()) return false;
        final int offset = e.pos().get().offset();
        if (offset < 0 || offset >= query.length() || query.charAt(offset) != '|') return false;

        int depth = 0;
        for (int i = offset - 1; i >= 0; i--) {
            final char c = query.charAt(i);
            if (c == ')' || c == ']' || c == '}') depth++;
            else if (c == '(' || c == '[' || c == '{') {
                if (depth == 0) return c == '(';
                depth--;
            }
        }
        return false;
    }

    private Iterable<CypherTranslatorStrategy> strategies;
}
