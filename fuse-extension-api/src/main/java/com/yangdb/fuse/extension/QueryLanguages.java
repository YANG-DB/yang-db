package com.yangdb.fuse.extension;

/*-
 * #%L
 * fuse-extension-api
 * %%
 * Copyright (C) 2016 - 2026 The YangDb Graph Database Project
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

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.QueryInfo;

/**
 * The registry of query languages: a Guice {@link MapBinder} from a language name to the transformer that turns a
 * query text into an {@link AsgQuery}.
 * <p>
 * The name is the query's {@code type} ({@code CreateQueryRequestMetadata.TYPE_*} for the built-in languages:
 * {@code cypher}, {@code sparql} (also {@code sparkQL}), {@code graphQL}) and the {@code <language>} path segment of the generic query
 * routes. Register from any Guice module:
 * <pre>{@code
 * QueryLanguages.register(binder, "echo").to(EchoTransformer.class);
 * }</pre>
 * Registering the same name twice fails at startup.
 */
public final class QueryLanguages {
    /** the transformer type every language binds: query text (plus ontology and type) to an ASG query */
    public static final TypeLiteral<QueryTransformer<QueryInfo<String>, AsgQuery>> TRANSFORMER =
            new TypeLiteral<QueryTransformer<QueryInfo<String>, AsgQuery>>() {};

    private QueryLanguages() {
    }

    /** the language map binder; declaring it is idempotent, so any module may call this */
    public static MapBinder<String, QueryTransformer<QueryInfo<String>, AsgQuery>> mapBinder(Binder binder) {
        return MapBinder.newMapBinder(binder, TypeLiteral.get(String.class), TRANSFORMER);
    }

    /** starts the binding of one language: finish it with {@code .to(...)}, {@code .toInstance(...)} etc. */
    public static LinkedBindingBuilder<QueryTransformer<QueryInfo<String>, AsgQuery>> register(Binder binder, String language) {
        return mapBinder(binder).addBinding(language);
    }
}
