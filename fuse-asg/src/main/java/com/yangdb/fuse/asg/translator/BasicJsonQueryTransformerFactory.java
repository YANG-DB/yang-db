package com.yangdb.fuse.asg.translator;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.query.JsonQueryTransformerFactory;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.resourceInfo.FuseError;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Looks a query language up in the language registry: the
 * {@link com.yangdb.fuse.extension.QueryLanguages} map binder, keyed by query type
 * ({@code CreateQueryRequestMetadata.TYPE_*} for the built-in cypher, sparql and graphQL languages, which their
 * {@code Asg*Module}s register). Extensions add languages the same way.
 */
public class BasicJsonQueryTransformerFactory implements JsonQueryTransformerFactory {
    public static final String NO_TRANSLATOR = "No Query translator found";

    private final Map<String, QueryTransformer<QueryInfo<String>, AsgQuery>> languages;

    @Inject
    public BasicJsonQueryTransformerFactory(Map<String, QueryTransformer<QueryInfo<String>, AsgQuery>> languages) {
        this.languages = languages;
    }

    @Override
    public QueryTransformer<QueryInfo<String>, AsgQuery> transform(String type) {
        QueryTransformer<QueryInfo<String>, AsgQuery> transformer = type == null ? null : languages.get(type);
        if (transformer == null && type != null) {
            // tolerate the case of a language name in a URL (/fuse/query/graphql/run)
            transformer = languages.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(type))
                    .map(Map.Entry::getValue)
                    .findFirst().orElse(null);
        }
        if (transformer == null) {
            throw new FuseError.FuseErrorException(new FuseError(NO_TRANSLATOR,
                    "No matching json query translator found for type " + type + "; available languages: " + languages()));
        }
        return transformer;
    }

    @Override
    public Set<String> languages() {
        return Collections.unmodifiableSet(new TreeSet<>(languages.keySet()));
    }
}
