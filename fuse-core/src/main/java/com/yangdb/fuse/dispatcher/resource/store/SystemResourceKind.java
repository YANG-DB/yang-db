package com.yangdb.fuse.dispatcher.resource.store;

/*-
 * #%L
 * fuse-core
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

import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import java.util.Collections;
import java.util.Map;

/**
 * Tells apart the kinds of resource documents YANG-DB keeps in its system indices
 * ({@code fuse_system}: stored queries, {@code fuse_node_info}: node status reports).
 * <p>
 * Up to OpenSearch 1.3 these documents were written under the mapping type {@code "resource"}, and reads were
 * restricted to that type. OpenSearch 2.x removes mapping types, so the kind is now an explicit keyword field
 * ({@link #FIELD}) that every write sets and every read filters on.
 * <p>
 * Compatibility: documents written by a 1.3-era YANG-DB have no {@link #FIELD}. They were all of the single kind
 * their index holds, so a document <b>without</b> the field matches every kind ({@link #filter}, {@link #matches}).
 * An in-place upgrade therefore needs no reindex of the system indices.
 */
public final class SystemResourceKind {
    /**
     * the discriminator field
     */
    public static final String FIELD = "kind";
    /**
     * a stored query resource (index {@code fuse_system})
     */
    public static final String QUERY = "query";
    /**
     * a node status report (index {@code fuse_node_info})
     */
    public static final String NODE = "node";

    private SystemResourceKind() {
    }

    /**
     * query that keeps documents of the given kind, plus legacy (pre-2.x) documents that carry no kind
     */
    public static QueryBuilder filter(String kind) {
        return QueryBuilders.boolQuery()
                .should(QueryBuilders.termQuery(FIELD, kind))
                .should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(FIELD)))
                .minimumShouldMatch(1);
    }

    /**
     * true when a fetched document source is of the given kind (or is a legacy document with no kind)
     */
    public static boolean matches(Map<String, Object> source, String kind) {
        if (source == null) return false;
        Object value = source.get(FIELD);
        return value == null || kind.equals(value.toString());
    }

    /**
     * untyped mapping for a newly created system index: the kind is an exact-match keyword
     */
    public static Map<String, Object> mapping() {
        return Collections.singletonMap("properties",
                Collections.singletonMap(FIELD, Collections.singletonMap("type", "keyword")));
    }
}
