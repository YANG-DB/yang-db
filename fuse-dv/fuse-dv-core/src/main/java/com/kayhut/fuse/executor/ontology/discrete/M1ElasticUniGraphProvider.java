package com.kayhut.fuse.executor.ontology.discrete;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.executor.ontology.UniGraphProvider;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.ElementController;
import com.kayhut.fuse.unipop.controller.common.logging.LoggingReduceController;
import com.kayhut.fuse.unipop.controller.common.logging.LoggingSearchController;
import com.kayhut.fuse.unipop.controller.common.logging.LoggingSearchVertexController;
import com.kayhut.fuse.unipop.controller.discrete.DiscreteElementReduceController;
import com.kayhut.fuse.unipop.controller.discrete.DiscreteElementVertexController;
import com.kayhut.fuse.unipop.controller.discrete.DiscreteVertexController;
import com.kayhut.fuse.unipop.controller.discrete.DiscreteVertexFilterController;
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
    public M1ElasticUniGraphProvider(
            Client client,
            ElasticGraphConfiguration elasticGraphConfiguration,
            UniGraphConfiguration uniGraphConfiguration,
            GraphElementSchemaProviderFactory schemaProviderFactory,
            SearchOrderProviderFactory orderProvider,
            MetricRegistry metricRegistry) {
        this.client = client;
        this.elasticGraphConfiguration = elasticGraphConfiguration;
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
                                        new DiscreteElementVertexController(client, elasticGraphConfiguration, uniGraph, schemaProvider, orderProvider),
                                        metricRegistry),
                                null
                        ),
                        new LoggingSearchVertexController(
                                new DiscreteVertexController(client, elasticGraphConfiguration, uniGraph, schemaProvider, orderProvider),
                                metricRegistry),
                        new LoggingSearchVertexController(
                                new DiscreteVertexFilterController(client, elasticGraphConfiguration, uniGraph, schemaProvider, orderProvider),
                                metricRegistry),
                        new LoggingReduceController(
                                new DiscreteElementReduceController(client, elasticGraphConfiguration, uniGraph, schemaProvider),
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
    private final ElasticGraphConfiguration elasticGraphConfiguration;
    private final UniGraphConfiguration uniGraphConfiguration;
    private final GraphElementSchemaProviderFactory schemaProviderFactory;
    private SearchOrderProviderFactory orderProvider;
    private MetricRegistry metricRegistry;
    //endregion
}
