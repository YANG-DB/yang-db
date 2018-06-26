package com.kayhut.fuse.unipop.controller.common.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.dispatcher.logging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.query.aggregation.ReduceQuery;

import java.util.Collections;

import static com.codahale.metrics.MetricRegistry.name;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogType.failure;
import static com.kayhut.fuse.dispatcher.logging.LogType.start;
import static com.kayhut.fuse.dispatcher.logging.LogType.success;

public class LoggingReduceController implements ReduceQuery.SearchController {
    //region Constructors
    public LoggingReduceController(
            ReduceQuery.SearchController searchController,
            MetricRegistry metricRegistry) {
        this.logger = LoggerFactory.getLogger(searchController.getClass());
        this.searchController = searchController;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    //region ReduceQuery.SearchController Implementation
    @Override
    public long count(ReduceQuery reduceQuery) {
        Timer.Context timerContext = this.metricRegistry.timer(name(this.logger.getName(), count.toString())).time();
        boolean thrownException = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start count", sequence, LogType.of(start), count, ElapsedFrom.now()).log();
            return searchController.count(reduceQuery);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed count", sequence, LogType.of(failure), count, ElapsedFrom.now())
                    .with(ex).log();
            this.metricRegistry.meter(name(this.logger.getName(), count.toString(), "failure")).mark();
            return 0;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, trace, "finish count", sequence, LogType.of(success), count, ElapsedFrom.now()).log();
                this.metricRegistry.meter(name(this.logger.getName(), count.toString(), "success")).mark();
            }
            timerContext.stop();
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private ReduceQuery.SearchController searchController;

    private static MethodName.MDCWriter count = MethodName.of("count");
    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
