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

public class ExclusiveChainTranslator extends CompositeQueryTranslator {
    public ExclusiveChainTranslator(PredicateQueryTranslator... translators) {
        super(translators);
    }

    public ExclusiveChainTranslator(Iterable<PredicateQueryTranslator> translators) {
        super(translators);
    }

    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        for (PredicateQueryTranslator predicateQueryTranslator : translators) {
            if(predicateQueryTranslator.test(key, predicate)) {
                queryBuilder = predicateQueryTranslator.translate(queryBuilder, key, predicate);
                break;
            }
        }

        return queryBuilder;
    }

}
