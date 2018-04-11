package com.kayhut.fuse.services.controllers;

import com.kayhut.fuse.model.transport.ContentResponse;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public interface IdGeneratorController<TId> {
    ContentResponse<TId> getNext(String genName, int numIds);
}
