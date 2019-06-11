package com.kayhut.fuse.unipop.controller.common.logging;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.codahale.metrics.MetricRegistry;
import com.kayhut.fuse.dispatcher.decorators.MethodDecorator;
import com.kayhut.fuse.dispatcher.decorators.MethodDecorator.ResultHandler.Passthrough;
import com.kayhut.fuse.dispatcher.logging.*;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unipop.process.Profiler;
import org.unipop.query.search.SearchQuery;

import java.util.Collections;
import java.util.Iterator;

import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;

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
        return new LoggingSyncMethodDecorator<Iterator<E>>(this.logger, this.metricRegistry, search, trace)
                .decorate(() -> this.searchController.search(searchQuery), new Passthrough<>((ex) -> Collections.emptyIterator()));
    }

    @Override
    public Profiler getProfiler() {
        return this.searchController.getProfiler();
    }

    @Override
    public void setProfiler(Profiler profiler) {
        this.searchController.setProfiler(profiler);
    }
    //endregion

    //region Fields
    private Logger logger;
    private MetricRegistry metricRegistry;
    private SearchQuery.SearchController searchController;

    private static MethodName.MDCWriter search = MethodName.of("search");
    //endregion
}
