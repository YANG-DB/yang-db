package com.kayhut.fuse.executor.ontology;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.promise.PromiseElementController;
import com.kayhut.fuse.unipop.controller.promise.PromiseVertexController;
import com.kayhut.fuse.unipop.controller.promise.PromiseVertexFilterController;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;
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
            PhysicalIndexProviderFactory physicalIndexProviderFactory,
            GraphLayoutProviderFactory graphLayoutProviderFactory) {
        this.client = client;
        this.elasticGraphConfiguration = elasticGraphConfiguration;
        this.uniGraphConfiguration = uniGraphConfiguration;
        this.physicalIndexProviderFactory = physicalIndexProviderFactory;
        this.graphLayoutProviderFactory = graphLayoutProviderFactory;
    }
    //endregion

    @Override
    public UniGraph getGraph(Ontology ontology) throws Exception {
        GraphElementSchemaProvider schemaProvider = new OntologySchemaProvider(
                ontology,
                this.physicalIndexProviderFactory.get(ontology),
                this.graphLayoutProviderFactory.get(ontology));

        return new UniGraph(this.uniGraphConfiguration, controllerManagerFactory(schemaProvider), new StandardStrategyProvider());
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
                        new PromiseElementController(client, elasticGraphConfiguration, uniGraph, schemaProvider,metricRegistry),
                        new PromiseVertexController(client, elasticGraphConfiguration, uniGraph, schemaProvider,metricRegistry),
                        new PromiseVertexFilterController(client, elasticGraphConfiguration, uniGraph, schemaProvider ,metricRegistry)
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
    private final PhysicalIndexProviderFactory physicalIndexProviderFactory;
    private final GraphLayoutProviderFactory graphLayoutProviderFactory;
    //endregion
}
