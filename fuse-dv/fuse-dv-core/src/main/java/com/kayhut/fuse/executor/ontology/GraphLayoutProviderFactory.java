package com.kayhut.fuse.executor.ontology;

import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.GraphLayoutProvider;

/**
 * Created by Roman on 23/05/2017.
 */
public interface GraphLayoutProviderFactory {
    GraphLayoutProvider get(Ontology ontology);
}
