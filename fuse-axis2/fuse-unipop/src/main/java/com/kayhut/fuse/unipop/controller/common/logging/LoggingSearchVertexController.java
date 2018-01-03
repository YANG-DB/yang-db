package com.kayhut.fuse.unipop.controller.common.logging;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.query.search.SearchVertexQuery;

import java.util.Collections;
import java.util.Iterator;

/**
 * Created by Roman on 12/14/2017.
 */
public class LoggingSearchVertexController implements SearchVertexQuery.SearchVertexController {
    //region Constructors
    public LoggingSearchVertexController(SearchVertexQuery.SearchVertexController searchVertexController) {
        this.logger = LoggerFactory.getLogger(searchVertexController.getClass());
        this.searchVertexController = searchVertexController;
    }
    //endregion

    //region SearchVertexQuery.SearchVertexController Implementation
    @Override
    public Iterator<Edge> search(SearchVertexQuery searchVertexQuery) {
        boolean thrownException = false;

        try {
            this.logger.trace("start search");
            return searchVertexController.search(searchVertexQuery);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed search", ex);
            return Collections.emptyIterator();
        } finally {
            if (!thrownException) {
                this.logger.trace("finish search");
            }
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private SearchVertexQuery.SearchVertexController searchVertexController;
    //endregion
}
