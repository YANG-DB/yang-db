package org.unipop.process.predicate;

/*-
 * #%L
 * unipop-core
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

import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.Objects;

/**
 * Created by Roman on 11/9/2015.
 *
 * Example of a terms count query with range filter on amount of terms
 * {
 *   "size": 0,
 *   "query": {
 *     "bool": {
 *       "filter": [
 *         {
 *           "term": {
 *             "direction": "out"
 *           }
 *         }
 *       ]
 *     }
 *   },
 *   "aggs": {
 *     "edges": {
 *       "terms": {
 *         "field": "entityA.id"
 *       },
 *       "aggs": {
 *         "range_bucket_filter": {
 *           "bucket_selector": {
 *             "buckets_path": {
 *               "edgeCount": "_count"
 *             },
 *             "script": "def a=params.edgeCount; a > 405 && a < 567"
 *           }
 *         }
 *       },
 *       "meta": {
 *         "key": "entityA.id"
 *       }
 *     }
 *   }
 * }
 */
public class CountP<V> extends P<V> {
    private final Type type;
    private final Compare compare;

    enum Type {
        terms,
        count,
        min,
        max,
        avg,
        stats,
        histogram,
        cardinality
    }

    //region Constructors
    private CountP(Type type, Compare compare, V value) {
        super(null, value);
        this.type = type;
        this.compare = compare;
    }

    public Type getType() {
        return type;
    }

    public Compare getCompare() {
        return compare;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CountP<?> countP = (CountP<?>) o;
        return type == countP.type && compare == countP.compare;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, compare);
    }
//endregion
}
