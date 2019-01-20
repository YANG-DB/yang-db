package com.kayhut.fuse.unipop.controller.search.translation;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.elasticsearch.common.geo.GeoPoint;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Roman on 18/05/2017.
 */
public class ContainsGeoBoundsQueryTranslator implements PredicateQueryTranslator {
    private String[] geoFields;
    //region PredicateQueryTranslator Implementation

    public ContainsGeoBoundsQueryTranslator(String... geoFields) {
        this.geoFields = geoFields;
    }

    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, String key, P<?> predicate) {
        if (predicate == null) {
            return queryBuilder;
        }

        if (!(predicate.getBiPredicate() instanceof Contains)
                || !Arrays.asList(geoFields).contains(key)
                || !(predicate.getValue() instanceof List)) {
            return queryBuilder;
        }

        Contains contains = (Contains) predicate.getBiPredicate();
        final List box = (List) predicate.getValue();
        switch (contains) {
            case within:
                if (CollectionUtil.listFromObjectValue(predicate.getValue()).isEmpty()) {
                    queryBuilder.push().bool().mustNot().exists(key).pop();
                } else {
                    queryBuilder.push()
                            .geoBoundingBox("geo_bounds", key,new GeoPoint(box.get(0).toString()), new GeoPoint(box.get(1).toString()))
                            .pop();
                }
                break;
            case without:
                if (CollectionUtil.listFromObjectValue(predicate.getValue()).isEmpty()) {
                    queryBuilder.push().exists(key).pop();
                } else {
                    queryBuilder.push().bool().mustNot()
                            .geoBoundingBox("geo_bounds", key,new GeoPoint(box.get(0).toString()), new GeoPoint(box.get(1).toString()))
                            .pop();
                }
                break;
        }

        return queryBuilder;
    }
    //endregion
}
