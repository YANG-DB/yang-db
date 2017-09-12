package com.kayhut.fuse.unipop.controller.promise.appender;

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;

/**
 * Created by User on 27/03/2017.
 */
public abstract class SearchQueryAppenderBase<TContext> implements SearchAppender<TContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, TContext context) {
        return append(searchBuilder.getQueryBuilder(), context);
    }
    //endregion

    //region Abstract Methods
    protected abstract boolean append(QueryBuilder queryBuilder, TContext context);
    //endregion
}
