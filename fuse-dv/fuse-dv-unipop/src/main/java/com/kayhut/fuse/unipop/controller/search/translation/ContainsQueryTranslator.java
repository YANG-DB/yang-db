package com.kayhut.fuse.unipop.controller.search.translation;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.Collections;

/**
 * Created by Roman on 18/05/2017.
 */
public class ContainsQueryTranslator implements PredicateQueryTranslator {
    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        if (predicate == null) {
            return queryBuilder;
        }

        if (!(predicate.getBiPredicate() instanceof Contains)) {
            return queryBuilder;
        }

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
    //endregion
}
