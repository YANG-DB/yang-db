package com.kayhut.fuse.unipop.controller.common.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.dispatcher.logging.*;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.query.search.SearchQuery;

import java.util.Collections;
import java.util.Iterator;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

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
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), search.toString())).time();
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start search", sequence, LogType.of(start), search, ElapsedFrom.now()).log();
            return searchController.search(searchQuery);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed search", sequence, LogType.of(failure), search, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), search.toString(), "failure")).mark();
            return Collections.emptyIterator();
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, trace, "finish search", sequence, LogType.of(success), search, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), search.toString(), "success")).mark();
            }
            timerContext.stop();
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private SearchQuery.SearchController searchController;

    private static MethodName.MDCWriter search = MethodName.of("search");
    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
