package com.kayhut.fuse.executor.ontology;

import com.google.inject.Inject;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;

/**
 * Created by Roman on 25/05/2017.
 */
public class OntologyGraphElementSchemaProviderFactory implements GraphElementSchemaProviderFactory {
    //region Constructors
    @Inject
    public OntologyGraphElementSchemaProviderFactory(
            PhysicalIndexProviderFactory physicalIndexProviderFactory,
            GraphLayoutProviderFactory graphLayoutProviderFactory) {
        this.physicalIndexProviderFactory = physicalIndexProviderFactory;
        this.graphLayoutProviderFactory = graphLayoutProviderFactory;
    }
    //endregion

    //region GraphElementSchemaProviderFactory Implementation
    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        return new OntologySchemaProvider(
                ontology,
                this.physicalIndexProviderFactory.get(ontology),
                this.graphLayoutProviderFactory.get(ontology));
    }
    //endregion

    //region Fields
    private PhysicalIndexProviderFactory physicalIndexProviderFactory;
    private GraphLayoutProviderFactory graphLayoutProviderFactory;
    //endregion
}
