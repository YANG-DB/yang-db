package com.kayhut.fuse.executor.ontology;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.OntologySchemaProvider;

/**
 * Created by Roman on 25/05/2017.
 */
public class OntologyGraphElementSchemaProviderFactory implements GraphElementSchemaProviderFactory {
    public static final String schemaProviderFactoryParameter = "OntologyGraphElementSchemaProviderFactory.@schemaProviderFactory";

    //region Constructors
    @Inject
    public OntologyGraphElementSchemaProviderFactory(
            @Named(schemaProviderFactoryParameter) GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.schemaProviderFactory = schemaProviderFactory;
    }
    //endregion

    //region GraphElementSchemaProviderFactory Implementation
    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        return new OntologySchemaProvider(ontology, this.schemaProviderFactory.get(ontology));
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    //endregion
}
