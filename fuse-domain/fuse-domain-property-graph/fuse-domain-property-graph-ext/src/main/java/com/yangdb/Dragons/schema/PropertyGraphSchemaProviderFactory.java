package com.yangdb.Dragons.schema;

import com.yangdb.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.unipop.schemaProviders.GraphElementSchemaProvider;

import java.util.HashMap;
import java.util.Map;

public class PropertyGraphSchemaProviderFactory implements GraphElementSchemaProviderFactory {

    public PropertyGraphSchemaProviderFactory() {
        this.schemaProviders = new HashMap<>();
    }

    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        return schemaProviders.get(ontology.getOnt());
    }

    private Map<String, GraphElementSchemaProvider> schemaProviders;
}
