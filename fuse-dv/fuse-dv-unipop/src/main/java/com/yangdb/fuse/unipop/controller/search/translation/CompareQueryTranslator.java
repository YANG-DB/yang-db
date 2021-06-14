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

import com.yangdb.fuse.unipop.controller.search.AggregationBuilder;
import com.yangdb.fuse.unipop.controller.search.QueryBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.P;

/**
 * Created by Roman on 18/05/2017.
 */
public class CompareQueryTranslator implements PredicateQueryTranslator {
    //region Constructors
    public CompareQueryTranslator() {

    }

    public CompareQueryTranslator(boolean shouldAggregateRange) {
        this.shouldAggregateRange = shouldAggregateRange;
    }
    //endregion

    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate) {
        Compare compare = (Compare) predicate.getBiPredicate();
        String rangeName = shouldAggregateRange ? key : null;
        switch (compare) {
            case eq:
                queryBuilder.push().term(key, predicate.getValue()).pop();
                break;
            case neq:
                queryBuilder.push().bool().mustNot().term(key, predicate.getValue()).pop();
                break;
            case gt:
                queryBuilder.push().range(rangeName, key).from(predicate.getValue()).includeLower(false).pop();
                break;
            case gte:
                queryBuilder.push().range(rangeName, key).from(predicate.getValue()).includeLower(true).pop();
                break;
            case lt:
                queryBuilder.push().range(rangeName, key).to(predicate.getValue()).includeUpper(false).pop();
                break;
            case lte:
                queryBuilder.push().range(rangeName, key).to(predicate.getValue()).includeUpper(true).pop();
                break;
        }

        return queryBuilder;
    }
    //endregion

    @Override
    public boolean test(String key, P<?> predicate) {
        return (predicate != null) && (predicate.getBiPredicate() instanceof Compare);
    }

    //region Fields
    private boolean shouldAggregateRange;
    //endregion
}
