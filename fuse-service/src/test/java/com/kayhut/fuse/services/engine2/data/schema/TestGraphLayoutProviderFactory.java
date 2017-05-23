package com.kayhut.fuse.services.engine2.data.schema;

import com.kayhut.fuse.executor.uniGraphProvider.GraphLayoutProviderFactory;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.GraphLayoutProvider;

/**
 * Created by Roman on 23/05/2017.
 */
public class TestGraphLayoutProviderFactory implements GraphLayoutProviderFactory {
    //region GraphLayoutProviderFactory Implementation
    @Override
    public GraphLayoutProvider get(Ontology ontology) {
        return new GraphLayoutProvider.NoneRedundant();
    }
    //endregion
}
