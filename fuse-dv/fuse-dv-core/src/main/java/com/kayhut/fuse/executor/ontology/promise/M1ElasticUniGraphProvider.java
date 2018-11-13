package com.kayhut.fuse.executor.ontology.promise;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.executor.ontology.UniGraphProvider;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.ElementController;
import com.kayhut.fuse.unipop.controller.common.logging.LoggingSearchController;
import com.kayhut.fuse.unipop.controller.common.logging.LoggingSearchVertexController;
import com.kayhut.fuse.unipop.controller.promise.PromiseElementEdgeController;
import com.kayhut.fuse.unipop.controller.promise.PromiseElementVertexController;
import com.kayhut.fuse.unipop.controller.promise.PromiseVertexController;
import com.kayhut.fuse.unipop.controller.promise.PromiseVertexFilterController;
import com.kayhut.fuse.unipop.controller.search.SearchOrderProvider;
import com.kayhut.fuse.unipop.controller.search.SearchOrderProviderFactory;
import com.kayhut.fuse.unipop.process.traversal.strategy.FuseStandardStrategyProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.structure.FuseUniGraph;
import org.elasticsearch.client.Client;
import org.unipop.configuration.UniGraphConfiguration;
import org.unipop.process.strategyregistrar.StandardStrategyProvider;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.ControllerManagerFactory;
import org.unipop.query.controller.UniQueryController;
import org.unipop.structure.UniGraph;

import java.util.Set;

/**
 * Created by Roman on 06/04/2017.
 */
public class M1ElasticUniGraphProvider implements UniGraphProvider {
    //region Constructors

    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    public M1ElasticUniGraphProvider(
            Client client,
            ElasticGraphConfiguration elasticGraphConfiguration,
            UniGraphConfiguration uniGraphConfiguration,
            GraphElementSchemaProviderFactory schemaProviderFactory,
            SearchOrderProviderFactory orderProviderFactory) {
        this.client = client;
        this.elasticGraphConfiguration = elasticGraphConfiguration;
        this.uniGraphConfiguration = uniGraphConfiguration;
        this.schemaProviderFactory = schemaProviderFactory;
        this.orderProviderFactory = orderProviderFactory;
    }
    //endregion

    @Override
    public UniGraph getGraph(Ontology ontology) throws Exception {
        return new FuseUniGraph(
                this.uniGraphConfiguration,
                controllerManagerFactory(schemaProviderFactory.get(ontology)),
                new FuseStandardStrategyProvider());
    }

    //region Private Methods
    /**
     * default controller Manager
     * @return
     */
    private ControllerManagerFactory controllerManagerFactory(GraphElementSchemaProvider schemaProvider) {
        return uniGraph -> new ControllerManager() {
            @Override
            public Set<UniQueryController> getControllers() {
                return ImmutableSet.of(
                        new ElementController(
                                new LoggingSearchController(
                                        new PromiseElementVertexController(client, elasticGraphConfiguration, uniGraph, schemaProvider, orderProviderFactory), metricRegistry),
                                new LoggingSearchController(
                                        new PromiseElementEdgeController(client, elasticGraphConfiguration, uniGraph, schemaProvider), metricRegistry)),
                        new LoggingSearchVertexController(
                                new PromiseVertexController(client, elasticGraphConfiguration, uniGraph, schemaProvider), metricRegistry),
                        new LoggingSearchVertexController(
                                new PromiseVertexFilterController(client, elasticGraphConfiguration, uniGraph, schemaProvider, orderProviderFactory), metricRegistry)
                );
            }

            @Override
            public void close() {
            }
        };
    }
    //endregion

    //region Fields
    private final Client client;
    private final ElasticGraphConfiguration elasticGraphConfiguration;
    private final UniGraphConfiguration uniGraphConfiguration;
    private final GraphElementSchemaProviderFactory schemaProviderFactory;
    private final SearchOrderProviderFactory orderProviderFactory;
    //endregion
}
