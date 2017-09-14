package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.search.SearchBuilder;

/**
 * Created by User on 27/03/2017.
 */
public interface SearchAppender<TContext> {
    boolean append(SearchBuilder searchBuilder, TContext context);
}
