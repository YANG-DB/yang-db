package com.yangdb.fuse.unipop.schemaProviders.indexPartitions;

/*-
 * #%L
 * fuse-dv-unipop
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Range-bucket semantics of a time partition's {@code values}.
 * <p>
 * Each value is the <b>inclusive upper bound</b> of its bucket and the first bucket is open below; values are
 * compared numerically, digit group by digit group (so "500" &lt; "1000" and "2017-2" &lt; "2017-10").
 * With values 500/1000/1500/2000 and date format yyyy: years up to 500 go to _500, 501..1000 to _1000,
 * 1001..1500 to _1500, 1501..2000 to _2000, and a later year fits no bucket.
 */
public final class TimeSeriesBuckets {
    private static final Pattern DIGITS = Pattern.compile("\\d+");

    private TimeSeriesBuckets() {}

    /**
     * orders index names by the numeric value of their bucket
     */
    public static Comparator<String> byBucket(String indexFormat) {
        return (a, b) -> compareKeys(key(valueOf(a, indexFormat)), key(valueOf(b, indexFormat)));
    }

    /**
     * the index of the bucket covering the (already date-formatted) value: the smallest bucket whose upper bound
     * is &gt;= the value; empty when the value is after the last bucket
     */
    public static Optional<String> bucketOf(Iterable<String> indices, String indexFormat, String formattedValue) {
        long[] value = key(formattedValue);
        return sorted(indices, indexFormat).stream()
                .filter(index -> compareKeys(key(valueOf(index, indexFormat)), value) >= 0)
                .findFirst();
    }

    public static List<String> sorted(Iterable<String> indices, String indexFormat) {
        return StreamSupport.stream(indices.spliterator(), false)
                .sorted(byBucket(indexFormat))
                .collect(Collectors.toList());
    }

    /**
     * the bucket value part of an index name built with the given index format (e.g. "idx_fire_%s")
     */
    static String valueOf(String index, String indexFormat) {
        if (indexFormat == null || !indexFormat.contains("%s"))
            return index;
        String prefix = indexFormat.substring(0, indexFormat.indexOf("%s"));
        String suffix = indexFormat.substring(indexFormat.indexOf("%s") + 2);
        if (index.length() >= prefix.length() + suffix.length()
                && index.regionMatches(true, 0, prefix, 0, prefix.length())
                && index.regionMatches(true, index.length() - suffix.length(), suffix, 0, suffix.length()))
            return index.substring(prefix.length(), index.length() - suffix.length());
        return index;
    }

    static long[] key(String value) {
        List<Long> groups = new ArrayList<>();
        Matcher matcher = DIGITS.matcher(value);
        while (matcher.find())
            groups.add(Long.parseLong(matcher.group()));
        return groups.stream().mapToLong(Long::longValue).toArray();
    }

    private static int compareKeys(long[] a, long[] b) {
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            int result = Long.compare(a[i], b[i]);
            if (result != 0)
                return result;
        }
        return Integer.compare(a.length, b.length);
    }
}
