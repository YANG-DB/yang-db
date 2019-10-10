package com.yangdb.fuse.unipop.controller.search.translation;

/*-
 *
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.unipop.controller.search.QueryBuilder;
import com.yangdb.fuse.unipop.controller.utils.CollectionUtil;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.elasticsearch.common.geo.GeoPoint;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Roman on 18/05/2017.
 */
public class ContainsGeoDistanceQueryTranslator implements PredicateQueryTranslator {
    public static final String GEO_DISTANCE = "geo_distance";
    private String[] geoFields;
    //region PredicateQueryTranslator Implementation

    public ContainsGeoDistanceQueryTranslator(String... geoFields) {
        this.geoFields = geoFields;
    }

    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        Contains contains = (Contains) predicate.getBiPredicate();
        final List box = (List) predicate.getValue();
        switch (contains) {
            case within:
                if (CollectionUtil.listFromObjectValue(predicate.getValue()).isEmpty()) {
                    queryBuilder.push().bool().mustNot().exists(key).pop();
                } else {
                    queryBuilder.push()
                            //box.get(0) - geo function name (geo_distance)
                            .geoDistance(GEO_DISTANCE, key, new GeoPoint(box.get(1).toString()), box.get(2).toString())
                            .pop();
                }
                break;
            case without:
                if (CollectionUtil.listFromObjectValue(predicate.getValue()).isEmpty()) {
                    queryBuilder.push().exists(key).pop();
                } else {
                    queryBuilder.push().bool().mustNot()
                            //box.get(0) - geo function name (geo_distance)
                            .geoDistance(GEO_DISTANCE, key, new GeoPoint(box.get(1).toString()), box.get(2).toString())
                            .pop();
                }
                break;
        }

        return queryBuilder;
    }
    //endregion


    @Override
    public boolean test(String key, P<?> predicate) {
        return (predicate != null) && ((predicate.getBiPredicate() instanceof Contains) && (predicate.getValue() instanceof List)
                && ((List) predicate.getValue()).get(0).equals(GEO_DISTANCE)
                && Arrays.asList(geoFields).contains(key)

        );
    }
}
