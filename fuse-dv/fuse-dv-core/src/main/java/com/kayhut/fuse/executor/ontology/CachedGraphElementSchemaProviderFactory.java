package com.kayhut.fuse.executor.ontology;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;

import java.util.HashMap;
import java.util.Map;

public class CachedGraphElementSchemaProviderFactory implements GraphElementSchemaProviderFactory {
    public static final String schemaProviderFactoryParameter = "CachedGraphElementSchemaProviderFactory.@schemaProviderFactory";

    //region Constructors
    @Inject
    public CachedGraphElementSchemaProviderFactory(
            @Named(schemaProviderFactoryParameter) GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.schemaProviderFactory = schemaProviderFactory;
        this.schemaProviders = new HashMap<>();
        this.sync = new Object();
    }
    //endregion

    //region GraphElementSchemaProviderFactory Implementation
    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        synchronized (this.sync) {
            return this.schemaProviders.computeIfAbsent(ontology.getOnt(),
                    ont -> new GraphElementSchemaProvider.Cached(this.schemaProviderFactory.get(ontology)));
        }
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory schemaProviderFactory;

    private Map<String, GraphElementSchemaProvider> schemaProviders;
    private Object sync;
    //endregion
}
