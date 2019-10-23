package com.yangdb.fuse.unipop.controller.search.translation;

/*-
 * #%L
 * fuse-dv-unipop
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

import com.yangdb.fuse.unipop.controller.search.QueryBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.util.OrP;

/**
 * Created by Roman on 18/05/2017.
 */
public class OrPQueryTranslator extends CompositeQueryTranslator {
    //region Constructors
    public OrPQueryTranslator(PredicateQueryTranslator...translators) {
        super(translators);
    }

    public OrPQueryTranslator(Iterable<PredicateQueryTranslator> translators) {
        super(translators);
    }    //endregion

    //region CompositeQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        OrP<?> orP = (OrP<?>)predicate;
        queryBuilder.push().bool().should();
        for(P<?> innerPredicate : orP.getPredicates()) {
            queryBuilder.push();
            queryBuilder = super.translate(queryBuilder, key, innerPredicate);
            queryBuilder.pop();
        }

        return queryBuilder.pop();
    }
    //endregion

    @Override
    public boolean test(String key, P<?> predicate) {
        return (predicate instanceof OrP<?>);
    }
}
