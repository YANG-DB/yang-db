package com.kayhut.fuse.services.controllers;

import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;

/**
 * Created by lior on 19/02/2017.
 */
public interface SearchController {

    ContentResponse search(CreateQueryRequest request);

}
