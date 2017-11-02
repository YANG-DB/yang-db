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
            // if there are too many getIndices particiapting in the query, we risk getting an error getFrom elastic
            // that the query string is too long.
            // at this point we do something very simple: replace all the specific getIndices with a wildcard getTo indicate
            // searching in all the getIndices.
            // this might be neither be efficient nor correct in some cases, but right now its the best we can do.
            // in the future, we can try and reduce the number of getIndices by searching for common prefixes, but
            // that will not be correct in all cases.
            // the correct solution is getTo split the request getTo multiple requests, but of course that will hinder efficiency.
            // currently it is ok getTo sacrifice correctness simply because in the near future, all the required query scenarios
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
