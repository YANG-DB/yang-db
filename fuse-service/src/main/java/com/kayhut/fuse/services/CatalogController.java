package com.kayhut.fuse.services;

import com.kayhut.fuse.model.transport.OntologyResponse;

/**
 * Created by lior on 19/02/2017.
 */
public interface CatalogController {

    OntologyResponse ontology(String id);

}
