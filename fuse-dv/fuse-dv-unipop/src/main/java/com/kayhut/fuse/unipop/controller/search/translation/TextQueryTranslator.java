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
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.unipop.process.predicate.Text;

import java.util.List;

/**
 * Created by Roman on 18/05/2017.
 */
public class TextQueryTranslator implements PredicateQueryTranslator {
    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        Text.TextPredicate text = (Text.TextPredicate)predicate.getBiPredicate();
        switch (text) {
            case PREFIX:
                List<String> prefixes = CollectionUtil.listFromObjectValue(predicate.getValue());
                switch (prefixes.size()) {
                    case 0:
                        break;
                    case 1:
                        queryBuilder.push().prefix(key, prefixes.get(0)).pop();
                        break;
                    default:
                        queryBuilder.bool().should();
                        prefixes.forEach(prefix -> queryBuilder.push().prefix(key, prefix).pop());
                        queryBuilder.pop();
                }
                break;

            case REGEXP:
                List<String> regexs = CollectionUtil.listFromObjectValue(predicate.getValue());
                switch (regexs.size()) {
                    case 0:
                        break;
                    case 1:
                        queryBuilder.push().regexp(key, regexs.get(0)).pop();
                        break;
                    default:
                        queryBuilder.push().bool().should();
                        regexs.forEach(regex ->  queryBuilder.push().regexp(key, regex).pop());
                        queryBuilder.pop();
                }
                break;

            case LIKE:
                if (Iterable.class.isAssignableFrom(predicate.getValue().getClass())) {
                    queryBuilder.push().bool().should();
                    //chane default wildcard to wildcard script
                    ((Iterable)predicate.getValue()).forEach(likeValue -> queryBuilder.push().wildcardScript(key, likeValue.toString()).pop());
                    queryBuilder.pop();
                } else {
                    queryBuilder.push().wildcardScript(key, predicate.getValue().toString()).pop();
                }
                break;
        }

        return queryBuilder;
    }

    @Override
    public boolean test(String key, P<?> predicate) {
        return (predicate != null) && (predicate.getBiPredicate() instanceof Text.TextPredicate);
    }
    //endregion
}
