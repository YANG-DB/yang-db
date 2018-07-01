package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;

/**
 * Created by roman.margolis on 25/09/2017.
 */
public class NormalizeRoutingSearchAppender implements SearchAppender<ElementControllerContext> {
    //region Constructors
    public NormalizeRoutingSearchAppender(int maxNumValues) {
        this.maxNumValues = maxNumValues;
    }
    //endregion

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, ElementControllerContext context) {
        if (searchBuilder.getRouting().size() > maxNumValues) {
            searchBuilder.getRouting().clear();
            return true;
        }

        return false;
    }
    //endregion

    //region Fields
    private int maxNumValues;
    //endregion
}
