package com.kayhut.fuse.executor.ontology;

import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;

/**
 * Created by Roman on 25/05/2017.
 */
public interface GraphElementSchemaProviderFactory {
    GraphElementSchemaProvider get(Ontology ontology);
}
