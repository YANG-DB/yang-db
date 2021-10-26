package com.yangdb.fuse.executor.ontology.promise;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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
import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.executor.ontology.UniGraphProvider;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.unipop.controller.OpensearchGraphConfiguration;
import com.yangdb.fuse.unipop.controller.common.ElementController;
import com.yangdb.fuse.unipop.controller.common.logging.LoggingSearchController;
import com.yangdb.fuse.unipop.controller.common.logging.LoggingSearchVertexController;
import com.yangdb.fuse.unipop.controller.promise.PromiseElementEdgeController;
import com.yangdb.fuse.unipop.controller.promise.PromiseElementVertexController;
import com.yangdb.fuse.unipop.controller.promise.PromiseVertexController;
import com.yangdb.fuse.unipop.controller.promise.PromiseVertexFilterController;
import com.yangdb.fuse.unipop.controller.search.SearchOrderProviderFactory;
import com.yangdb.fuse.unipop.process.traversal.strategy.FuseStandardStrategyProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.yangdb.fuse.unipop.structure.FuseUniGraph;
import org.opensearch.client.Client;
import org.unipop.configuration.UniGraphConfiguration;
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
            OpensearchGraphConfiguration opensearchGraphConfiguration,
            UniGraphConfiguration uniGraphConfiguration,
            GraphElementSchemaProviderFactory schemaProviderFactory,
            SearchOrderProviderFactory orderProviderFactory) {
        this.client = client;
        this.opensearchGraphConfiguration = opensearchGraphConfiguration;
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
                                        new PromiseElementVertexController(client, opensearchGraphConfiguration, uniGraph, schemaProvider, orderProviderFactory,metricRegistry),
                                        metricRegistry),
                                new LoggingSearchController(
                                        new PromiseElementEdgeController(client, opensearchGraphConfiguration, uniGraph, schemaProvider,metricRegistry),
                                        metricRegistry)),
                        new LoggingSearchVertexController(
                                new PromiseVertexController(client, opensearchGraphConfiguration, uniGraph, schemaProvider,metricRegistry),
                                metricRegistry),
                        new LoggingSearchVertexController(
                                new PromiseVertexFilterController(client, opensearchGraphConfiguration, uniGraph, schemaProvider, orderProviderFactory,metricRegistry),
                                metricRegistry)
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
    private final OpensearchGraphConfiguration opensearchGraphConfiguration;
    private final UniGraphConfiguration uniGraphConfiguration;
    private final GraphElementSchemaProviderFactory schemaProviderFactory;
    private final SearchOrderProviderFactory orderProviderFactory;
    //endregion
}
