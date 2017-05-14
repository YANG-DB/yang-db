package com.kayhut.fuse.services.engine2.data.schema;

import com.kayhut.fuse.executor.uniGraphProvider.PhysicalIndexProviderFactory;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.PhysicalIndexProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roman on 11/05/2017.
 */
public class TestPhysicalIndexProviderFactory implements PhysicalIndexProviderFactory {
    //region Constructors
    public TestPhysicalIndexProviderFactory() {
        this.physicalIndexProviders = new HashMap<>();
        this.physicalIndexProviders.put("Dragons", new DragonsPhysicalIndexProvider());
    }
    //endregion

    //region PhysicalIndexProviderFactory implementation
    @Override
    public PhysicalIndexProvider get(Ontology ontology) {
        return this.physicalIndexProviders.get(ontology.getOnt());
    }
    //endregion

    //region Fields
    private Map<String, PhysicalIndexProvider> physicalIndexProviders;
    //endregion
}
