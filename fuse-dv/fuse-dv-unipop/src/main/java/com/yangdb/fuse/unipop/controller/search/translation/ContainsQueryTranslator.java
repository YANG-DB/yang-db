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
import com.yangdb.fuse.unipop.controller.utils.CollectionUtil;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.P;

/**
 * Created by Roman on 18/05/2017.
 */
public class ContainsQueryTranslator implements PredicateQueryTranslator {
    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate) {
        Contains contains = (Contains) predicate.getBiPredicate();
        switch (contains) {
            case within:
                if (CollectionUtil.listFromObjectValue(predicate.getValue()).isEmpty()) {
                    queryBuilder.push().bool().mustNot().exists(key).pop();
                } else {
                    queryBuilder.push().terms(key, predicate.getValue()).pop();
                }
                break;
            case without:
                if (CollectionUtil.listFromObjectValue(predicate.getValue()).isEmpty()) {
                    queryBuilder.push().exists(key).pop();
                } else {
                    queryBuilder.push().bool().mustNot().terms(key, predicate.getValue()).pop();
                }
                break;
        }

        return queryBuilder;
    }

    @Override
    public boolean test(String key, P<?> predicate) {
        return (predicate != null) && (predicate.getBiPredicate() instanceof Contains);
    }

    //endregion
}
