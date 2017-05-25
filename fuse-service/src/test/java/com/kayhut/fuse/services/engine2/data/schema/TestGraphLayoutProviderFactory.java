package com.kayhut.fuse.services.engine2.data.schema;

import com.kayhut.fuse.executor.uniGraphProvider.GraphLayoutProviderFactory;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.GraphLayoutProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roman on 23/05/2017.
 */
public class TestGraphLayoutProviderFactory implements GraphLayoutProviderFactory {
    //region Constructors
    public TestGraphLayoutProviderFactory() {
        this.graphLayoutProviders = new HashMap<>();
        this.graphLayoutProviders.put("Dragons", new DragonsGraphLayoutProvider());
    }
    //endregion

    //region GraphLayoutProviderFactory Implementation
    @Override
    public GraphLayoutProvider get(Ontology ontology) {
        return this.graphLayoutProviders.get(ontology.getOnt());
    }
    //endregion

    //region Fields
    private Map<String, GraphLayoutProvider> graphLayoutProviders;
    //endregion
}
