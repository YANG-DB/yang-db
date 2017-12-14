package com.kayhut.fuse.services.controllers;

import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;

/**
 * Created by lior on 19/02/2017.
 */
public interface CatalogController {

    ContentResponse<Ontology> getOntology(String id);

    ContentResponse<GraphElementSchemaProvider> getSchema(String id);
}
