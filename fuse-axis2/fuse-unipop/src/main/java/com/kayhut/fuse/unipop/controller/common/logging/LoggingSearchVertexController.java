package com.kayhut.fuse.unipop.controller.common.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.query.search.SearchVertexQuery;

import java.util.Collections;
import java.util.Iterator;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by Roman on 12/14/2017.
 */
public class LoggingSearchVertexController implements SearchVertexQuery.SearchVertexController {
    //region Constructors
    public LoggingSearchVertexController(
            SearchVertexQuery.SearchVertexController searchVertexController,
            MetricRegistry metricRegistry) {
        this.logger = LoggerFactory.getLogger(searchVertexController.getClass());
        this.metricRegistry = metricRegistry;
        this.searchVertexController = searchVertexController;
    }
    //endregion

    //region SearchVertexQuery.SearchVertexController Implementation
    @Override
    public Iterator<Edge> search(SearchVertexQuery searchVertexQuery) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "search")).time();

        boolean thrownException = false;

        try {
            this.logger.trace("start search");
            return searchVertexController.search(searchVertexQuery);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed search", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "search", "failure")).mark();
            return Collections.emptyIterator();
        } finally {
            if (!thrownException) {
                this.logger.trace("finish search");
                this.metricRegistry.meter(name(this.logger.getName(), "search", "success")).mark();
            }
            timerContext.stop();
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private SearchVertexQuery.SearchVertexController searchVertexController;
    //endregion
}
