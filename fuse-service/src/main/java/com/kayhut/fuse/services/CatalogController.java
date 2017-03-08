package com.kayhut.fuse.services;

import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.transport.ContentResponse;

/**
 * Created by lior on 19/02/2017.
 */
public interface CatalogController {

    ContentResponse<Ontology> get(String id);

}
