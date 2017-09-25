package com.kayhut.fuse.unipop.controller.common.appender;

import com.kayhut.fuse.unipop.controller.common.context.ElementControllerContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;

/**
 * Created by roman.margolis on 25/09/2017.
 */
public class NormalizeIndexSearchAppender implements SearchAppender<ElementControllerContext> {
    //region Constructors
    public NormalizeIndexSearchAppender(int maxNumValues) {
        this.maxNumValues = maxNumValues;
    }
    //endregion

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, ElementControllerContext context) {
        if (searchBuilder.getIndices().size() > maxNumValues) {
            searchBuilder.getIndices().clear();
            // if there are too many indices particiapting in the query, we risk getting an error from elastic
            // that the query string is too long.
            // at this point we do something very simple: replace all the specific indices with a wildcard to indicate
            // searching in all the indices.
            // this might be neither be efficient nor correct in some cases, but right now its the best we can do.
            // in the future, we can try and reduce the number of indices by searching for common prefixes, but
            // that will not be correct in all cases.
            // the correct solution is to split the request to multiple requests, but of course that will hinder efficiency.
            // currently it is ok to sacrifice correctness simply because in the near future, all the required query scenarios
            // will be correct under this policy, but further ahead this appender will be replaced.
            searchBuilder.getIndices().add("*");
            return true;
        }

        return false;
    }
    //endregion

    //region Fields
    private int maxNumValues;
    //endregion
}
