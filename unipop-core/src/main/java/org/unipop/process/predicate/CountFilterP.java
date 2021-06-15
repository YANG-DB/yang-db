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
import org.apache.tinkerpop.gremlin.process.traversal.util.ConnectiveP;

import java.util.Objects;
import java.util.function.BiPredicate;

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
public class CountFilterP<V> extends P<V> {


    public static <V> CountFilterP<V> eq(final V value) {
        return new CountFilterP<>(Compare.eq, value);
    }

    public static <V> CountFilterP<V> neq(final V value) {
        return new CountFilterP<>(Compare.neq, value);
    }

    public static <V> CountFilterP<V> lt(final V value) {
        return new CountFilterP<>(Compare.lt, value);
    }

    public static <V> CountFilterP<V> lte(final V value) {
        return new CountFilterP<>(Compare.lte, value);
    }

    public static <V> CountFilterP<V> gt(final V value) {
        return new CountFilterP<>(Compare.gt, value);
    }

    public static <V> CountFilterP<V> gte(final V value) {
        return new CountFilterP<>(Compare.gte, value);
    }

    public static <V> AndCountFilterP<V> between(final V first, final V second) {
        return new AndCountFilterP<V>(new CountFilterP<>(Compare.gte, first), new CountFilterP<>(Compare.lt, second));
    }

    public static class AndCountFilterP<V> extends ConnectiveP<V> {

        public AndCountFilterP(CountFilterP<V> ... predicates) {
            super(predicates);
        }

    }
    //region Constructors

    private CountFilterP( Compare compare, V value) {
        super((BiPredicate)compare, value);
    }



    
//endregion
}
