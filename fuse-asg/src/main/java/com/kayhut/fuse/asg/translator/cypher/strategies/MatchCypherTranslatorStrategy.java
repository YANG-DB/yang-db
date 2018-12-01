package com.kayhut.fuse.asg.translator.cypher.strategies;

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

import com.kayhut.fuse.asg.translator.cypher.strategies.expressions.WhereClauseNodeCypherTranslator;
import org.opencypher.v9_0.ast.*;
import org.opencypher.v9_0.expressions.PatternElement;
import org.opencypher.v9_0.expressions.PatternPart;
import scala.collection.Seq;

import java.util.Collection;
import java.util.Optional;

import static scala.collection.JavaConverters.asJavaCollectionConverter;

public class MatchCypherTranslatorStrategy implements CypherTranslatorStrategy {

    public MatchCypherTranslatorStrategy(Iterable<CypherElementTranslatorStrategy> strategies, WhereClauseNodeCypherTranslator whereClause) {
        this.strategies = strategies;
        this.whereClause = whereClause;
    }

    @Override
    public void apply(com.kayhut.fuse.model.query.Query query, CypherStrategyContext context) {
        final Statement statement = context.getStatement();
        if (!(statement instanceof Query)) return;

        Query cypherQuery = (Query) statement;
        final QueryPart part = cypherQuery.part();
        if (part instanceof SingleQuery) {
            final Seq<Clause> clauses = ((SingleQuery) part).clauses();
            final Optional<Clause> matchClause = asJavaCollectionConverter(clauses).asJavaCollection()
                    .stream().filter(c -> c.getClass().isAssignableFrom(Match.class)).findAny();

            if (!matchClause.isPresent()) return;
            //manage patterns
            final Match match = (Match) matchClause.get();
            final Collection<PatternPart> patternParts = asJavaCollectionConverter(match.pattern().patternParts()).asJavaCollection();
            patternParts.forEach(p->applyPattern(p.element(),context,query));

            //manage where clause
            if(!match.where().isEmpty()) {
                Where where = match.where().get();
                whereClause.apply(where,query,context);
            }
        }
    }

    protected void applyPattern(PatternElement patternPart, CypherStrategyContext context, com.kayhut.fuse.model.query.Query query) {
        strategies.forEach(s->s.apply(patternPart,query,context));
    }

    private Iterable<CypherElementTranslatorStrategy> strategies;
    private WhereClauseNodeCypherTranslator whereClause;
}
