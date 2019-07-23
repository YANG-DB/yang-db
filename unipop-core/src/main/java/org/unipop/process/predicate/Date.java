package org.unipop.process.predicate;

/*-
 * #%L
 * Date.java - unipop-core - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import org.apache.commons.lang.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.util.AndP;
import org.apache.tinkerpop.gremlin.process.traversal.util.OrP;

import java.util.function.BiPredicate;

/**
 * Created by sbarzilay on 8/18/16.
 */
public class Date {
    public static <V> P<V> eq(final V value) { return new P(DatePredicate.eq, value); }
    public static <V> P<V> neq(final V value) { return new P(DatePredicate.neq, value); }
    public static <V> P<V> lt(final V value) { return new P(DatePredicate.lt, value); }
    public static <V> P<V> gt(final V value) { return new P(DatePredicate.gt, value); }
    public static <V> P<V> gte(final V value) { return new P(DatePredicate.gte, value); }
    public static <V> P<V> lte(final V value) { return new P(DatePredicate.lte, value); }
    public static <V> P<V> inside(final V first, final V second) {
        return new AndP<>(new P(DatePredicate.gt, first), new P(DatePredicate.lt, second));
    }
    public static <V> P<V> outside(final V first, final V second) {
        return new OrP<>(new P(DatePredicate.lt, first), new P(DatePredicate.gt, second));
    }
    public static <V> P<V> between(final V first, final V second) {
        return new AndP<>(new P(DatePredicate.gte, first), new P(DatePredicate.lt, second));
    }

    public enum DatePredicate implements BiPredicate<Object, Object> {
        eq {
            @Override
            public boolean test(Object o, Object o2) {
                return ((java.util.Date)o).compareTo((java.util.Date) o2) == 0;
            }

            @Override
            public BiPredicate<Object, Object> negate() {
                return neq;
            }
        },
        neq {
            @Override
            public boolean test(Object o, Object o2) {
                return !negate().test(o, o2);
            }

            @Override
            public BiPredicate<Object, Object> negate() {
                return eq;
            }
        },
        gt {
            @Override
            public boolean test(Object o, Object o2) {
                return ((java.util.Date)o).compareTo((java.util.Date) o2) > 0;
            }

            @Override
            public BiPredicate<Object, Object> negate() {
                return lte;
            }
        },
        lt {
            @Override
            public boolean test(Object o, Object o2) {
                return ((java.util.Date)o).compareTo((java.util.Date) o2) < 0;
            }

            @Override
            public BiPredicate<Object, Object> negate() {
                return gte;
            }
        },
        gte {
            @Override
            public boolean test(Object o, Object o2) {
                return gt.or(eq).test(o, o2);
            }

            @Override
            public BiPredicate<Object, Object> negate() {
                return lt;
            }
        },
        lte {
            @Override
            public boolean test(Object o, Object o2) {
                return lt.or(eq).test(o, o2);
            }

            @Override
            public BiPredicate<Object, Object> negate() {
                return gt;
            }
        }
    }
}
