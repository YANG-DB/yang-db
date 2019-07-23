package org.unipop.process.predicate;

/*-
 * #%L
 * Text.java - unipop-core - yangdb - 2,016
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

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.function.BiPredicate;

/**
 * Created by sbarzilay on 12/15/15.
 */
public class Text {
    public static <V> P<V> queryString(final V value) {
        return new P(TextPredicate.QUERY_STRING, value);
    }

    public static <V> P<V> like(final V value) {
        return new P(TextPredicate.LIKE, value);
    }

    public static <V> P<V> match(final V value) {
        return new P(TextPredicate.MATCH, value);
    }

    public static <V> P<V> matchPhrase(final V value) {
        return new P(TextPredicate.MATCH, value);
    }

    public static <V> P<V> unmatchPhrase(final V value) {
        return new P(TextPredicate.UNMATCH_PHRASE, value);
    }

    public static <V> P<V> unmatch(final V value) {
        return new P(TextPredicate.UNMATCH, value);
    }

    public static <V> P<V> unlike(final V value) {
        return new P(TextPredicate.UNLIKE, value);
    }

    public static <V> P<V> regexp(final V value) {
        return new P(TextPredicate.REGEXP, value);
    }

    public static <V> P<V> unregexp(final V value) {
        return new P(TextPredicate.UNREGEXP, value);
    }

    public static <V> P<V> fuzzy(final V value) {
        return new P(TextPredicate.FUZZY, value);
    }

    public static <V> P<V> unfuzzy(final V value) {
        return new P(TextPredicate.UNFUZZY, value);
    }

    public static <V> P<V> prefix(final V value) {
        return new P(TextPredicate.PREFIX, value);
    }

    public static <V> P<V> unprefix(final V value) {
        return new P(TextPredicate.UNPREFIX, value);
    }

    public enum TextPredicate implements BiPredicate<Object, Object> {
        PREFIX {
            @Override
            public boolean test(final Object first, final Object second) {
                return first.toString().startsWith(second.toString());
            }

            /**
             * The negative of {@code LIKE} is {@link #UNLIKE}.
             */
            @Override
            public TextPredicate negate() {
                return UNREGEXP;
            }
        },
        UNPREFIX {
            @Override
            public boolean test(final Object first, final Object second) {
                return !negate().test(first, second);
            }

            /**
             * The negative of {@code LIKE} is {@link #UNLIKE}.
             */
            @Override
            public TextPredicate negate() {
                return PREFIX;
            }
        },
        LIKE {
            @Override
            public boolean test(final Object first, final Object second) {
                return first.toString().matches(second.toString().replace("?", ".?").replace("*", ".*?"));
            }

            /**
             * The negative of {@code LIKE} is {@link #UNLIKE}.
             */
            @Override
            public TextPredicate negate() {
                return UNLIKE;
            }
        },
        MATCH {
            @Override
            public boolean test(final Object first, final Object second) {
                return first.toString().matches(second.toString().replace("?", ".?").replace("*", ".*?"));
            }

            /**
             * The negative of {@code MATCH} is {@link #UNMATCH}.
             */
            @Override
            public TextPredicate negate() {
                return UNMATCH;
            }
        },
        MATCH_PHRASE {
            @Override
            public boolean test(final Object first, final Object second) {
                return first.toString().matches(second.toString().replace("?", ".?").replace("*", ".*?"));
            }

            /**
             * The negative of {@code MATCH_PHRASE} is {@link #UNMATCH_PHRASE}.
             */
            @Override
            public TextPredicate negate() {
                return UNMATCH_PHRASE;
            }
        },
        UNMATCH {
            @Override
            public boolean test(final Object first, final Object second) {
                return !negate().test(first, second);
            }

            /**
             * The negative of {@code UNMATCH} is {@link #MATCH}.
             */
            @Override
            public TextPredicate negate() {
                return MATCH;
            }
        },
        UNMATCH_PHRASE {
            @Override
            public boolean test(final Object first, final Object second) {
                return !negate().test(first, second);
            }

            /**
             * The negative of {@code UNMATCH_PHRASE} is {@link #MATCH_PHRASE}.
             */
            @Override
            public TextPredicate negate() {
                return UNMATCH_PHRASE;
            }
        },
        UNLIKE {
            @Override
            public boolean test(final Object first, final Object second) {
                return !negate().test(first, second);
            }

            /**
             * The negative of {@code UNLIKE} is {@link #LIKE}.
             */
            @Override
            public TextPredicate negate() {
                return LIKE;
            }
        },
        REGEXP {
            @Override
            public boolean test(final Object first, final Object second) {
                return first.toString().matches(second.toString());
            }

            /**
             * The negative of {@code REGEXP} is {@link #UNREGEXP}.
             */
            @Override
            public TextPredicate negate() {
                return UNREGEXP;
            }
        },
        UNREGEXP {
            @Override
            public boolean test(final Object first, final Object second) {
                return !negate().test(first, second);
            }

            /**
             * The negative of {@code UNRGEXP} is {@link #REGEXP}.
             */
            @Override
            public TextPredicate negate() {
                return REGEXP;
            }
        },
        FUZZY {
            @Override
            public boolean test(final Object first, final Object second) {
                int levenshteinDistance = StringUtils.getLevenshteinDistance(second.toString(), first.toString());
                if (levenshteinDistance <= 3)
                    return true;
                return false;
            }

            /**
             * The negative of {@code FUZZY} is {@link #UNFUZZY}.
             */
            @Override
            public TextPredicate negate() {
                return UNFUZZY;
            }
        },
        UNFUZZY {
            @Override
            public boolean test(final Object first, final Object second) {
                return !negate().test(first, second);
            }

            /**
             * The negative of {@code UNFUZZY} is {@link #FUZZY}.
             */
            @Override
            public TextPredicate negate() {
                return FUZZY;
            }
        },
        QUERY_STRING {
            @Override
            public boolean test(final Object first, final Object second) {
                return first.toString().matches(second.toString().replace("?", ".?").replace("*", ".*?"));
            }

            /**
             * The negative of {@code QUERY_STRING} is {@link #UN_QUERY_STRING}.
             */
            @Override
            public TextPredicate negate() {
                return UN_QUERY_STRING;
            }
        },
        UN_QUERY_STRING {
            @Override
            public boolean test(final Object first, final Object second) {
                return !negate().test(first, second);
            }

            /**
             * The negative of {@code UN_QUERY_STRING} is {@link #QUERY_STRING}.
             */
            @Override
            public TextPredicate negate() {
                return QUERY_STRING;
            }
        }
    }
}
