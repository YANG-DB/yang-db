package com.yangdb.fuse.unipop.controller.promise.appender;

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

import com.yangdb.fuse.unipop.controller.common.appender.SearchAppender;
import com.yangdb.fuse.unipop.controller.search.AggregationBuilder;
import com.yangdb.fuse.unipop.controller.search.QueryBuilder;
import com.yangdb.fuse.unipop.controller.search.SearchBuilder;

/**
 * Created by lior.perry on 27/03/2017.
 */
public abstract class SearchQueryAppenderBase<TContext> implements SearchAppender<TContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, TContext context) {
        return append(searchBuilder.getQueryBuilder(),searchBuilder.getAggregationBuilder() , context);
    }
    //endregion

    //region Abstract Methods
    protected abstract boolean append(QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, TContext context);
    //endregion
}
