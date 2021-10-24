package com.yangdb.fuse.executor.ontology.discrete;

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
import com.yangdb.fuse.unipop.controller.common.logging.LoggingReduceController;
import com.yangdb.fuse.unipop.controller.common.logging.LoggingSearchController;
import com.yangdb.fuse.unipop.controller.common.logging.LoggingSearchVertexController;
import com.yangdb.fuse.unipop.controller.discrete.*;
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
    public M1ElasticUniGraphProvider(
            Client client,
            OpensearchGraphConfiguration opensearchGraphConfiguration,
            UniGraphConfiguration uniGraphConfiguration,
            GraphElementSchemaProviderFactory schemaProviderFactory,
            SearchOrderProviderFactory orderProvider,
            MetricRegistry metricRegistry) {
        this.client = client;
        this.opensearchGraphConfiguration = opensearchGraphConfiguration;
        this.uniGraphConfiguration = uniGraphConfiguration;
        this.schemaProviderFactory = schemaProviderFactory;
        this.orderProvider = orderProvider;
        this.metricRegistry = metricRegistry;
    }
    //endregion

    @Override
    public UniGraph getGraph(Ontology ontology) throws Exception {
        return new FuseUniGraph(
                this.uniGraphConfiguration,
                controllerManagerFactory(this.schemaProviderFactory.get(ontology), this.metricRegistry),
                new FuseStandardStrategyProvider());
    }

    //region Private Methods

    /**
     * default controller Manager
     *
     * @return
     */
    private ControllerManagerFactory controllerManagerFactory(GraphElementSchemaProvider schemaProvider, MetricRegistry metricRegistry) {
        return uniGraph -> new ControllerManager() {
            @Override
            public Set<UniQueryController> getControllers() {
                return ImmutableSet.of(
                        new ElementController(
                                new LoggingSearchController(
                                        new DiscreteElementVertexController(client, opensearchGraphConfiguration, uniGraph, schemaProvider, orderProvider),
                                        metricRegistry),
                                null
                        ),
                        new LoggingSearchVertexController(
                                new DiscreteVertexController(client, opensearchGraphConfiguration, uniGraph, schemaProvider, orderProvider),
                                metricRegistry),
                        new LoggingSearchVertexController(
                                new DiscreteVertexFilterController(client, opensearchGraphConfiguration, uniGraph, schemaProvider, orderProvider),
                                metricRegistry),
                        new LoggingReduceController(
                                new DiscreteElementReduceController(client, opensearchGraphConfiguration, uniGraph, schemaProvider),
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
    private SearchOrderProviderFactory orderProvider;
    private MetricRegistry metricRegistry;
    //endregion
}
