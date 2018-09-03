package com.kayhut.fuse.unipop.controller.common.logging;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.kayhut.fuse.dispatcher.decorators.MethodDecorator;
import com.kayhut.fuse.dispatcher.decorators.MethodDecorator.ResultHandler.Passthrough;
import com.kayhut.fuse.dispatcher.logging.*;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.process.Profiler;
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
        return new LoggingSyncMethodDecorator<Iterator<Edge>>(this.logger, this.metricRegistry, search, trace)
                .decorate(() -> this.searchVertexController.search(searchVertexQuery), new Passthrough<>((ex) -> Collections.emptyIterator()));
    }

    @Override
    public Profiler getProfiler() {
        return this.searchVertexController.getProfiler();
    }

    @Override
    public void setProfiler(Profiler profiler) {
        this.searchVertexController.setProfiler(profiler);
    }
    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private SearchVertexQuery.SearchVertexController searchVertexController;

    private static MethodName.MDCWriter search = MethodName.of("search");
    //endregion
}
