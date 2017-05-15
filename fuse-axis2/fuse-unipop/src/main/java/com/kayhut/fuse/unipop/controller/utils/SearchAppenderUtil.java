package com.kayhut.fuse.unipop.controller.utils;

import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.controller.search.appender.SearchAppender;

/**
 * Created by Roman on 15/05/2017.
 */
public class SearchAppenderUtil {
    public static <TContext, SContext> SearchAppender<SContext> wrap(SearchAppender<TContext> searchAppender) {
        return (SearchBuilder searchBuilder, SContext context) -> searchAppender.append(searchBuilder, (TContext)context);
    }
}
