package com.kayhut.fuse.unipop.controller.search.appender;

import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import javaslang.collection.Stream;

/**
 * Created by User on 28/03/2017.
 */
public class CompositeSearchAppender<TContext> implements SearchAppender<TContext> {
    //region Constructors
    public CompositeSearchAppender(Iterable<SearchAppender<TContext>> searchAppenders) {
        this.searchAppenders = Stream.ofAll(searchAppenders).toList();
    }
    //endregion

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, TContext context) {
        for(SearchAppender<TContext> searchAppender : this.searchAppenders) {
            if (searchAppender.append(searchBuilder, context)) {
                return true;
            }
        }

        return false;
    }
    //endregion

    //region Fields
    private Iterable<SearchAppender<TContext>> searchAppenders;
    //endregion
}
