package com.kayhut.fuse.asg.translator.cypher;

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

import com.google.inject.Inject;
import com.kayhut.fuse.asg.translator.AsgTranslator;
import com.kayhut.fuse.asg.translator.cypher.strategies.CypherStrategyContext;
import com.kayhut.fuse.asg.translator.cypher.strategies.CypherTranslatorStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Start;
import org.opencypher.v9_0.ast.Statement;
import org.opencypher.v9_0.parser.CypherParser;
import scala.Option;

import java.util.Collection;

public class CypherTranslator implements AsgTranslator<String,Query> {

    //region Constructors
    @Inject
    public CypherTranslator(String ontology, Collection<CypherTranslatorStrategy> strategies) {
        this.ontology = ontology;
        this.strategies = strategies;
    }
    //endregion


    @Override
    public Query translate(String source) {
        final Query.Builder builder = Query.Builder.instance();
        builder.withOnt(ontology).withName("cypher_");
        final Query v1Query = builder.build();

        //Working with the first element
        Start start = new Start(0,1);

        //add start element
        v1Query.getElements().add(0,start);

        //translate cypher asci into cypher AST
        final Statement statement = new CypherParser().parse(source,Option.empty());
        final CypherStrategyContext context = new CypherStrategyContext(statement,start);

        //apply strategies
        strategies.iterator().forEachRemaining(cypherTranslatorStrategy -> cypherTranslatorStrategy.apply(v1Query, context));
        return v1Query;
    }

    private String ontology;
    private Iterable<CypherTranslatorStrategy> strategies;
}
