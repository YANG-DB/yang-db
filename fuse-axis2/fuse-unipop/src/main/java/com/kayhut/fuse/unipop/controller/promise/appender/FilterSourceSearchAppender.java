package com.kayhut.fuse.unipop.controller.promise.appender;

import com.kayhut.fuse.unipop.controller.promise.context.SelectContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.predicates.SelectP;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;

/**
 * Created by Roman on 24/05/2017.
 */
public class FilterSourceSearchAppender implements SearchAppender<SelectContext> {
    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, SelectContext context) {
        for(HasContainer selectPHasContainer : context.getSelectPHasContainers()) {
            searchBuilder = appendSelectP(searchBuilder, selectPHasContainer.getKey(), selectPHasContainer.getPredicate());
        }

        return true;
    }
    //endregion

    //region Private Methods
    private SearchBuilder appendSelectP(SearchBuilder searchBuilder, String name, P<?> predicate) {
        if (!(predicate.getBiPredicate() instanceof SelectP)) {
            return searchBuilder;
        }

        SelectP selectP = (SelectP)predicate.getBiPredicate();
        switch (selectP) {
            case raw:
                searchBuilder.getIncludeSourceFields().add(predicate.getValue().toString());
                break;
        }

        return searchBuilder;
    }
    //endregion
}
