package com.yangdb.fuse.unipop.controller.common.converter;

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

import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class SearchAggDataItem implements DataItem {
    //region Constructors
    public SearchAggDataItem(String key, MultiBucketsAggregation.Bucket searchHit) {
        this.key = key;
        this.searchHit = searchHit;
    }
    //endregion

    //region DataItem Implementation
    @Override
    public Object id() {
        return this.searchHit.getKey();
    }

    @Override
    public Map<String, Object> properties() {
        if (this.properties == null) {
            //the prop map key it the field and the bucket key is the FK id value
            this.properties = Collections.singletonMap(key, this.searchHit.getKey());
        }
        return this.properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchAggDataItem that = (SearchAggDataItem) o;
        return Objects.equals(key, that.key) && Objects.equals(searchHit, that.searchHit) && Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, searchHit, properties);
    }
    //endregion

    private String key;
    //region Fields
    private MultiBucketsAggregation.Bucket searchHit;
    private Map<String, Object> properties;
    //endregion

    /**
     * build a terms iterator based
     *
     * @param map
     * @return
     */
    public static List<SearchAggDataItem> build(Map<String, Aggregation> map) {
        if (map.entrySet().size() == 1) {
            Map.Entry<String, Aggregation> aggregationEntry = map.entrySet().iterator().next();
            if (aggregationEntry.getValue() instanceof StringTerms) {
                return ((StringTerms) aggregationEntry.getValue())
                        .getBuckets().stream().map(b -> new SearchAggDataItem(aggregationEntry.getKey(), b)).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
