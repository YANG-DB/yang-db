package com.kayhut.fuse.unipop.controller;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.kayhut.fuse.unipop.converter.CompositeConverter;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import org.elasticsearch.client.Client;
import org.unipop.process.strategyregistrar.StandardStrategyProvider;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.UniQueryController;
import org.unipop.structure.UniGraph;

import java.util.Set;

/**
 * Created by liorp on 4/2/2017.
 */
@Singleton
public class UniGraphProvider {
    private final Client client;
    private final ElasticGraphConfiguration configuration;
    private UniGraph graph;
    private final GraphElementSchemaProvider schemaProvider;
    private final CompositeConverter converter;

    @Inject
    public UniGraphProvider(Client client, ElasticGraphConfiguration configuration, GraphElementSchemaProvider schemaProvider, CompositeConverter converter) throws Exception {
        this.client = client;
        this.configuration = configuration;
        this.schemaProvider = schemaProvider;
        this.converter = converter;
        this.graph = new UniGraph(controllerManager(), new StandardStrategyProvider());
    }

    /**
     * default controller Manager
     * @return
     */
    private ControllerManager controllerManager() {
        return new ControllerManager() {
            @Override
            public Set<UniQueryController> getControllers() {
                return ImmutableSet.of(
                        new SearchPromiseElementController(client,configuration,graph,schemaProvider,converter),
                        new SearchPromiseVertexController(client,configuration,graph,schemaProvider,converter));
            }

            @Override
            public void close() {
            }
        };
    }

    public UniGraph getGraph() {
        return graph;
    }


}
