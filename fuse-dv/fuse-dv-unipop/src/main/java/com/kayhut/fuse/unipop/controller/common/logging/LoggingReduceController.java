package com.kayhut.fuse.unipop.controller.common.logging;

import com.codahale.metrics.MetricRegistry;
import com.kayhut.fuse.dispatcher.decorators.MethodDecorator;
import com.kayhut.fuse.dispatcher.decorators.MethodDecorator.ResultHandler.Passthrough;
import com.kayhut.fuse.dispatcher.logging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.query.aggregation.ReduceQuery;

import java.util.Collections;

import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;

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
        return new LoggingSyncMethodDecorator<Long>(this.logger, this.metricRegistry, count, trace)
                .decorate(() -> this.searchController.count(reduceQuery), new Passthrough<>((ex) -> 0L));
    }
    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private ReduceQuery.SearchController searchController;

    private static MethodName.MDCWriter count = MethodName.of("count");
    //endregion
}
