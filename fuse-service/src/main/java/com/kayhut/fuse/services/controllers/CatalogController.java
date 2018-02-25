package com.kayhut.fuse.services.controllers;

import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;

import java.util.List;

/**
 * Created by lior on 19/02/2017.
 */
public interface CatalogController {

    ContentResponse<Ontology> getOntology(String id);

    List<ContentResponse<Ontology>> getOntologies();

    ContentResponse<GraphElementSchemaProvider> getSchema(String id);

    List<ContentResponse> getSchemas();
}
