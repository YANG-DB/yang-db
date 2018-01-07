package com.kayhut.fuse.unipop.controller.common.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.query.search.SearchQuery;

import java.util.Collections;
import java.util.Iterator;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by Roman on 12/14/2017.
 */
public class LoggingSearchController implements SearchQuery.SearchController {
    //region Constructors
    public LoggingSearchController(
            SearchQuery.SearchController searchController,
            MetricRegistry metricRegistry) {
        this.logger = LoggerFactory.getLogger(searchController.getClass());
        this.metricRegistry = metricRegistry;
        this.searchController = searchController;
    }
    //endregion

    //region SearchQuery.SearchController Implementation
    @Override
    public <E extends Element> Iterator<E> search(SearchQuery<E> searchQuery) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), "search")).time();

        boolean thrownExcpetion = false;

        try {
            this.logger.trace("start search");
            return searchController.search(searchQuery);
        } catch (Exception ex) {
            thrownExcpetion = true;
            this.logger.error("failed search", ex);
            this.metricRegistry.meter(name(this.logger.getName(), "search", "failure")).mark();
            return Collections.emptyIterator();
        } finally {
            if (!thrownExcpetion) {
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
    private SearchQuery.SearchController searchController;
    //endregion
}
