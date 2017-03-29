package com.kayhut.fuse.unipop.controller.search.appender;

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;

/**
 * Created by User on 27/03/2017.
 */
public class GlobalTypeSearchAppender<PromiseElementControllerContext> extends SearchQueryAppenderBase<PromiseElementControllerContext> {
    //region SearchQueryAppenderBase Implementation
    @Override
    public boolean append(QueryBuilder queryBuilder, PromiseElementControllerContext context) {
        return false;
    }
    //endregion
}
