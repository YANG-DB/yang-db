package com.kayhut.fuse.executor.uniGraphProvider;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.SearchPromiseElementController;
import com.kayhut.fuse.unipop.controller.SearchPromiseVertexController;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.PhysicalIndexProvider;
import org.elasticsearch.client.Client;
import org.unipop.process.strategyregistrar.StandardStrategyProvider;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.controller.ControllerManagerFactory;
import org.unipop.query.controller.UniQueryController;
import org.unipop.structure.UniGraph;

import java.util.Set;

/**
 * Created by Roman on 06/04/2017.
 */
public class ElasticUniGraphProvider implements UniGraphProvider {
    //region Constructors
    @Inject
    public ElasticUniGraphProvider(
            Client client,
            ElasticGraphConfiguration configuration,
            PhysicalIndexProvider physicalIndexProvider) {
        this.client = client;
        this.configuration = configuration;
        this.physicalIndexProvider = physicalIndexProvider;
    }
    //endregion

    @Override
    public UniGraph getGraph(Ontology ontology) throws Exception {
        GraphElementSchemaProvider schemaProvider = new OntologySchemaProvider(this.physicalIndexProvider, ontology);
        return new UniGraph(controllerManagerFactory(schemaProvider), new StandardStrategyProvider());
    }

    //region Private Methods
    /**
     * default controller Manager
     * @return
     */
    private ControllerManagerFactory controllerManagerFactory(GraphElementSchemaProvider schemaProvider) {
        return new ControllerManagerFactory() {
            @Override
            public ControllerManager create(UniGraph uniGraph) {
                return new ControllerManager() {
                    @Override
                    public Set<UniQueryController> getControllers() {
                        return ImmutableSet.of(
                                new SearchPromiseElementController(client, configuration, uniGraph, schemaProvider),
                                new SearchPromiseVertexController(client, configuration, uniGraph, schemaProvider));
                    }

                    @Override
                    public void close() {
                    }
                };
            }
        };
    }
    //endregion

    //region Fields
    private final Client client;
    private final ElasticGraphConfiguration configuration;
    private final PhysicalIndexProvider physicalIndexProvider;
    //endregion
}
