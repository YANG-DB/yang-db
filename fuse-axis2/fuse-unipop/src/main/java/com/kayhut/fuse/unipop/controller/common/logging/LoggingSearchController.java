package com.kayhut.fuse.unipop.controller.common.logging;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.query.search.SearchQuery;

import java.util.Collections;
import java.util.Iterator;

/**
 * Created by Roman on 12/14/2017.
 */
public class LoggingSearchController implements SearchQuery.SearchController {
    //region Constructors
    public LoggingSearchController(SearchQuery.SearchController searchController) {
        this.logger = LoggerFactory.getLogger(searchController.getClass());
        this.searchController = searchController;
    }
    //endregion

    //region SearchQuery.SearchController Implementation
    @Override
    public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
        boolean thrownExcpetion = false;

        try {
            this.logger.trace("start search");
            return searchController.search(searchQuery);
        } catch (Exception ex) {
            thrownExcpetion = true;
            this.logger.error("failed search", ex);
            return Collections.emptyIterator();
        } finally {
            if (!thrownExcpetion) {
                this.logger.trace("finish search");
            }
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private SearchQuery.SearchController searchController;
    //endregion
}
