package com.yangdb.fuse.asg.translator.sparql.strategies;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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

import com.yangdb.fuse.model.asgQuery.AsgQuery;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.Var;

public class StepPatternTranslatorStrategy implements SparqlElementTranslatorStrategy{

    @Override
    public void apply(TupleExpr element, AsgQuery query, SparqlStrategyContext context) {
        //todo verify edge creation (type & relation)
        if(StatementPattern.class.isAssignableFrom(element.getClass())) {
            Var objectVar = ((StatementPattern) element).getObjectVar();
            Var predicateVar = ((StatementPattern) element).getPredicateVar();
            Var subjectVar = ((StatementPattern) element).getSubjectVar();
            Var contextVar = ((StatementPattern) element).getContextVar();
        }
    }
}
