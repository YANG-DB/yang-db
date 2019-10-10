package org.unipop.query.predicates;

/*-
 *
 * PredicateQuery.java - unipop-core - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.unipop.query.StepDescriptor;
import org.unipop.query.UniQuery;

public class PredicateQuery<E extends Element> extends UniQuery {
    private final PredicatesHolder predicates;

    public PredicateQuery(PredicatesHolder predicates, StepDescriptor stepDescriptor) {
        super(stepDescriptor);
        this.predicates = predicates;
    }

    public PredicatesHolder getPredicates(){
        return predicates;
    }

    public boolean test(E element, PredicatesHolder predicates) {
        if(predicates.getClause().equals(PredicatesHolder.Clause.And)) {
            if (!HasContainer.testAll(element, predicates.getPredicates())) return false;

            for (PredicatesHolder child : predicates.getChildren()) {
                if (!test(element, child)) return false;
            }
            return true;
        }
        else {
            for(HasContainer has : predicates.getPredicates()) {
                if (has.test(element)) return true;
            }
            for (PredicatesHolder child : predicates.getChildren()) {
                if (test(element, child)) return true;
            }
            return false;
        }
    }

    @Override
    public String toString() {
        return "PredicateQuery{" +
                "predicates=" + predicates +
                '}';
    }
}
