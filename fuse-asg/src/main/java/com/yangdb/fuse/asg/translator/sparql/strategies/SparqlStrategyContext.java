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
import com.yangdb.fuse.model.query.EBase;
import org.eclipse.rdf4j.query.parser.ParsedQuery;

import java.util.Optional;

public class SparqlStrategyContext {

    public SparqlStrategyContext(ParsedQuery statement, AsgEBase<? extends EBase> scope) {
        this.statement = statement;
        this.scope = scope;
        this.where = Optional.empty();
    }

    public AsgEBase<? extends EBase> getScope() {
        return scope;
    }

    public ParsedQuery getStatement() {
        return statement;
    }


    public SparqlStrategyContext scope(AsgEBase<? extends EBase> scope) {
        this.scope = scope;
        return this;
    }

    public Optional<com.bpodgursky.jbool_expressions.Expression> getWhere() {
        return where;
    }

    //region Fields
    private ParsedQuery statement;
    private Optional<com.bpodgursky.jbool_expressions.Expression> where ;

    private AsgEBase<? extends EBase> scope;

    //endregion
}
