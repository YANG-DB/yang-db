package com.yangdb.fuse.asg.translator.sparql;

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
import com.yangdb.fuse.asg.strategy.SparqlAsgStrategyRegistrar;
import com.yangdb.fuse.asg.translator.AsgTranslator;
import com.yangdb.fuse.asg.translator.sparql.strategies.SparqlStrategyContext;
import com.yangdb.fuse.asg.translator.sparql.strategies.SparqlTranslatorStrategy;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.semanticweb.owlapi.model.IRI;

public class SparqlTranslator implements AsgTranslator<QueryInfo<String>, AsgQuery> {

    @Inject
    public SparqlTranslator(OntologyProvider provider,SparqlAsgStrategyRegistrar strategies) {
        this.provider = provider;
        this.strategies = strategies.register();
    }
    //endregion


    @Override
    public AsgQuery translate(QueryInfo<String> source) {
        Ontology ontology = provider.get(source.getOntology())
                .orElseThrow(() -> new FuseError.FuseErrorException(new FuseError("No Ontology present for Id ", "No Ontology present for id[" + source.getOntology()+"]")));

        final AsgQuery query = AsgQuery.Builder.start("sparql_", source.getOntology()).build();

        //translate cypher asci into cypher AST
        ParsedQuery statement = new SPARQLParser().parseQuery(source.getQuery(), IRI.create(query.getOnt()).toString());
        final SparqlStrategyContext context = new SparqlStrategyContext(ontology,statement,query, query.getStart());

        //todo implement projection fields
//        query.setProjectedFields(populate);

        //apply strategies
        strategies.iterator().forEachRemaining(strategy -> strategy.apply(query, context));
        return query;
    }

    private OntologyProvider provider;
    private Iterable<SparqlTranslatorStrategy> strategies;

}
