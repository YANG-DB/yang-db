package com.kayhut.fuse.services.controllers;

import com.kayhut.fuse.model.transport.ContentResponse;

/**
 * Created by lior on 19/02/2017.
 */
public interface DataLoaderController {

    ContentResponse<String> init(String ontology);
    ContentResponse<String> load(String ontology);
    ContentResponse<String> drop(String ontology);
}
