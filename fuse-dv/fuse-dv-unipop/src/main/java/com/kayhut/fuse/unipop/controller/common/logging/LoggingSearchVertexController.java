package com.kayhut.fuse.unipop.controller.common.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.dispatcher.logging.*;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.query.search.SearchVertexQuery;

import java.util.Collections;
import java.util.Iterator;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

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
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), search.toString())).time();
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start search", sequence, LogType.of(start), search, ElapsedFrom.now()).log();
            return searchVertexController.search(searchVertexQuery);
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
    private SearchVertexQuery.SearchVertexController searchVertexController;

    private static MethodName.MDCWriter search = MethodName.of("search");
    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
