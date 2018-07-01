package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import javaslang.collection.Stream;

import java.util.List;

/**
 * Created by roman.margolis on 28/12/2017.
 */
public class MustFetchSourceSearchAppender implements SearchAppender<ElementControllerContext> {
    //region Constructors
    public MustFetchSourceSearchAppender(String...mustFetchFields) {
        this(Stream.of(mustFetchFields));
    }

    public MustFetchSourceSearchAppender(Iterable<String> mustFetchFields) {
        this.mustFetchFields = Stream.ofAll(mustFetchFields).toJavaList();
    }
    //endregion

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, ElementControllerContext context) {
        searchBuilder.getIncludeSourceFields().addAll(this.mustFetchFields);
        return true;
    }
    //endregion

    //region Fields
    private List<String> mustFetchFields;
    //endregion
}
