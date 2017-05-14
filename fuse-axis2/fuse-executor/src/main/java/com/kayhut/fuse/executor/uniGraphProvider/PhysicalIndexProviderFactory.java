package com.kayhut.fuse.executor.uniGraphProvider;

import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.unipop.schemaProviders.PhysicalIndexProvider;

/**
 * Created by Roman on 11/05/2017.
 */
public interface PhysicalIndexProviderFactory {
    PhysicalIndexProvider get(Ontology ontology);
}
