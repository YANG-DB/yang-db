package com.kayhut.fuse.unipop.controller.search.appender;

import com.kayhut.fuse.unipop.controller.search.SearchBuilder;

/**
 * Created by User on 27/03/2017.
 */
public class IndexSearchAppender<PromiseElementControllerContext> implements SearchAppender<PromiseElementControllerContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, PromiseElementControllerContext promiseElementControllerContext) {
        return false;
    }
    //endregion
}
