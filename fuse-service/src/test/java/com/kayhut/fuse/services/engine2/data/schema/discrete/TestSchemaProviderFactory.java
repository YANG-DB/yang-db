package com.kayhut.fuse.services.engine2.data.schema.discrete;

import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.executor.ontology.schema.RawElasticSchema;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by roman.margolis on 28/09/2017.
 */
public class TestSchemaProviderFactory implements GraphElementSchemaProviderFactory{
    //region Constructors
    public TestSchemaProviderFactory(RawElasticSchema schema) {
        this.schemaProviders = new HashMap<>();
        this.schemaProviders.put("Dragons", new DragonsPhysicalSchemaProvider());
    }
    //endregion

    //region GraphLayoutProviderFactory Implementation
    @Override
    public GraphElementSchemaProvider get(Ontology ontology) {
        return this.schemaProviders.get(ontology.getOnt());
    }
    //endregion

    //region Fields
    private Map<String, GraphElementSchemaProvider> schemaProviders;
    //endregion
}
