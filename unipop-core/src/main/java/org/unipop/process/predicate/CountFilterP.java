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

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.util.ConnectiveP;

import java.math.BigDecimal;
import java.util.function.BiPredicate;

/**
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

    public enum CountFilterCompare implements BiPredicate<Object, Object> {

        eq {
            @Override
            public boolean test(final Object first, final Object second) {
                return null == first ? null == second : (first instanceof Number && second instanceof Number
                        && !first.getClass().equals(second.getClass())
                        ? big((Number) first).compareTo(big((Number) second)) == 0
                        : first.equals(second));
            }

            /**
             * The negative of {@code eq} is {@link #neq}.
             */
            @Override
            public CountFilterCompare negate() {
                return neq;
            }
        },

        /**
         * Evaluates if the first object is not equal to the second.  If both are of type {@link Number} but not of the
         * same class (i.e. double for the first object and long for the second object) both values are converted to
         * {@link BigDecimal} so that it can be evaluated via {@link BigDecimal#equals}.  Otherwise they are evaluated
         * via {@link Object#equals(Object)}.  Testing against {@link Number#doubleValue()} enables the compare
         * operations to be a bit more forgiving with respect to comparing different number types.
         */
        neq {
            @Override
            public boolean test(final Object first, final Object second) {
                return !eq.test(first, second);
            }

            /**
             * The negative of {@code neq} is {@link #eq}
             */
            @Override
            public CountFilterCompare negate() {
                return eq;
            }
        },

        /**
         * Evaluates if the first object is greater than the second.  If both are of type {@link Number} but not of the
         * same class (i.e. double for the first object and long for the second object) both values are converted to
         * {@link BigDecimal} so that it can be evaluated via {@link BigDecimal#compareTo}.  Otherwise they are evaluated
         * via {@link Comparable#compareTo(Object)}.  Testing against {@link BigDecimal#compareTo} enables the compare
         * operations to be a bit more forgiving with respect to comparing different number types.
         */
        gt {
            @Override
            public boolean test(final Object first, final Object second) {
                return null != first && null != second && (
                        first instanceof Number && second instanceof Number && !first.getClass().equals(second.getClass())
                                ? big((Number) first).compareTo(big((Number) second)) > 0
                                : ((Comparable) first).compareTo(second) > 0);
            }

            /**
             * The negative of {@code gt} is {@link #lte}.
             */
            @Override
            public CountFilterCompare negate() {
                return lte;
            }
        },

        /**
         * Evaluates if the first object is greater-equal to the second.  If both are of type {@link Number} but not of the
         * same class (i.e. double for the first object and long for the second object) both values are converted to
         * {@link BigDecimal} so that it can be evaluated via {@link BigDecimal#compareTo}.  Otherwise they are evaluated
         * via {@link Comparable#compareTo(Object)}.  Testing against {@link BigDecimal#compareTo} enables the compare
         * operations to be a bit more forgiving with respect to comparing different number types.
         */
        gte {
            @Override
            public boolean test(final Object first, final Object second) {
                return null == first ? null == second : (null != second && !lt.test(first, second));
            }

            /**
             * The negative of {@code gte} is {@link #lt}.
             */
            @Override
            public CountFilterCompare negate() {
                return lt;
            }
        },

        /**
         * Evaluates if the first object is less than the second.  If both are of type {@link Number} but not of the
         * same class (i.e. double for the first object and long for the second object) both values are converted to
         * {@link BigDecimal} so that it can be evaluated via {@link BigDecimal#compareTo}.  Otherwise they are evaluated
         * via {@link Comparable#compareTo(Object)}.  Testing against {@link BigDecimal#compareTo} enables the compare
         * operations to be a bit more forgiving with respect to comparing different number types.
         */
        lt {
            @Override
            public boolean test(final Object first, final Object second) {
                return null != first && null != second && (
                        first instanceof Number && second instanceof Number && !first.getClass().equals(second.getClass())
                                ? big((Number) first).compareTo(big((Number) second)) < 0
                                : ((Comparable) first).compareTo(second) < 0);
            }

            /**
             * The negative of {@code lt} is {@link #gte}.
             */
            @Override
            public CountFilterCompare negate() {
                return gte;
            }
        },

        /**
         * Evaluates if the first object is less-equal to the second.  If both are of type {@link Number} but not of the
         * same class (i.e. double for the first object and long for the second object) both values are converted to
         * {@link BigDecimal} so that it can be evaluated via {@link BigDecimal#compareTo}.  Otherwise they are evaluated
         * via {@link Comparable#compareTo(Object)}.  Testing against {@link BigDecimal#compareTo} enables the compare
         * operations to be a bit more forgiving with respect to comparing different number types.
         */
        lte {
            @Override
            public boolean test(final Object first, final Object second) {
                return null == first ? null == second : (null != second && !gt.test(first, second));
            }

            /**
             * The negative of {@code lte} is {@link #gt}.
             */
            @Override
            public CountFilterCompare negate() {
                return gt;
            }
        };

        /**
         * Produce the opposite representation of the current {@code Compare} enum.
         */
        @Override
        public abstract CountFilterCompare negate();

        /**
         * Convert Number to BigDecimal.
         */
        private static BigDecimal big(final Number n) {
            return new BigDecimal(n.toString());
        }
    }

    public static <V> CountFilterP<V> eq(final V value) {
        return new CountFilterP<>(CountFilterCompare.eq, value);
    }

    public static <V> CountFilterP<V> neq(final V value) {
        return new CountFilterP<>(CountFilterCompare.neq, value);
    }

    public static <V> CountFilterP<V> lt(final V value) {
        return new CountFilterP<>(CountFilterCompare.lt, value);
    }

    public static <V> CountFilterP<V> lte(final V value) {
        return new CountFilterP<>(CountFilterCompare.lte, value);
    }

    public static <V> CountFilterP<V> gt(final V value) {
        return new CountFilterP<>(CountFilterCompare.gt, value);
    }

    public static <V> CountFilterP<V> gte(final V value) {
        return new CountFilterP<>(CountFilterCompare.gte, value);
    }

    public static <V> AndCountFilterP<V> between(final V first, final V second) {
        return new AndCountFilterP<V>(new CountFilterP<V>(CountFilterCompare.gte, first), new CountFilterP<V>(CountFilterCompare.lt, second));
    }

    public static class AndCountFilterP<V> extends ConnectiveP<V> {

        public AndCountFilterP(CountFilterP<V> ... predicates) {
            super(predicates);
        }

    }
    //region Constructors

    private CountFilterP( CountFilterCompare compare, V value) {
        super((BiPredicate)compare, value);
    }



    
//endregion
}
