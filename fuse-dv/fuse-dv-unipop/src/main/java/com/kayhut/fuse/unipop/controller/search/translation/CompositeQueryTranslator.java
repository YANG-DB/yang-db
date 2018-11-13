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
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.Collections;

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
        for(PredicateQueryTranslator translator : this.translators) {
            queryBuilder = translator.translate(queryBuilder, key, predicate);
        }

        return queryBuilder;
    }
    //endregion

    //region Fields
    protected Iterable<PredicateQueryTranslator> translators;
    //endregion
}
