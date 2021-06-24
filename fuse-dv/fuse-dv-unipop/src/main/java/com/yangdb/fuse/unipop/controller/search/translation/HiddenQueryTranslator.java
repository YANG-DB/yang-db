package com.yangdb.fuse.unipop.controller.search.translation;

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

import com.yangdb.fuse.model.GlobalConstants;
import com.yangdb.fuse.unipop.controller.search.AggregationBuilder;
import com.yangdb.fuse.unipop.controller.search.QueryBuilder;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Graph;

/**
 * Created by Roman on 18/05/2017.
 */
public class HiddenQueryTranslator extends CompositeQueryTranslator {
    //region Constructors
    public HiddenQueryTranslator(Iterable<PredicateQueryTranslator> translators) {
        super(translators);
    }

    public HiddenQueryTranslator(PredicateQueryTranslator...translators) {
        super(translators);
    }
    //endregion

    //region Override Methods
    @Override
    public QueryBuilder translate(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String key, P<?> predicate) {
        String plainKey = Graph.Hidden.isHidden(key) ? Graph.Hidden.unHide(key) : key;
        String newKey;

        switch (plainKey) {
            case "id":
                newKey = GlobalConstants._ID;
                break;

            case "label":
                newKey = "type";
                break;

            default:
                newKey = plainKey;
        }

        return super.translate(queryBuilder, aggregationBuilder, newKey, predicate);
    }
    //endregion
}
