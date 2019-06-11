package com.kayhut.fuse.unipop.controller.search.translation;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.List;

/**
 * Created by Roman on 18/05/2017.
 */
public class CompositeQueryTranslator implements PredicateQueryTranslator {
    //region Constructors
    public CompositeQueryTranslator(PredicateQueryTranslator...translators) {
        this.translators = Stream.of(translators).toJavaList();
    }

    public CompositeQueryTranslator(Iterable<PredicateQueryTranslator> translators) {
        this.translators = Stream.ofAll(translators).toJavaList();
    }
    //endregion

    //region PredicateQueryTranslator Implementation
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        List<PredicateQueryTranslator> translators = Stream.ofAll(this.translators).filter(t -> t.test(key, predicate)).toJavaList();
        for (PredicateQueryTranslator predicateQueryTranslator : translators) {
            queryBuilder = predicateQueryTranslator.translate(queryBuilder, key, predicate);
        }

        return queryBuilder;
    }

    @Override
    public boolean test(String key, P<?> predicate) {
        return true;
    }

    //endregion

    //region Fields
    protected Iterable<PredicateQueryTranslator> translators;
    //endregion
}
