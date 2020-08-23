package com.yangdb.fuse.asg.translator.sparql.strategies;

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

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.EBase;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

public class SparqlStrategyContext {

    public SparqlStrategyContext(Ontology ontology, ParsedQuery statement, AsgQuery query,AsgEBase<? extends EBase> scope) {
        this.ontology = new Ontology.Accessor(ontology);
        this.statement = statement;
        this.query = query;
        this.scope = scope;
    }

    public AsgEBase<? extends EBase> getScope() {
        return scope;
    }

    public ParsedQuery getStatement() {
        return statement;
    }

    public Ontology.Accessor getOntology() {
        return ontology;
    }

    public SparqlStrategyContext scope(AsgEBase<? extends EBase> scope) {
        this.scope = scope;
        return this;
    }

    public AsgQuery getQuery() {
        return query;
    }

    private Ontology.Accessor ontology;
    //region Fields
    private ParsedQuery statement;

    private AsgQuery query;
    private AsgEBase<? extends EBase> scope;

    //endregion
}
