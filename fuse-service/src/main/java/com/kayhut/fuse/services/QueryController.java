package com.kayhut.fuse.services;

import com.kayhut.fuse.model.transport.Request;
import com.kayhut.fuse.model.transport.ContentResponse;

/**
 * Created by lior on 19/02/2017.
 */
public interface QueryController {

    ContentResponse query(Request request);


}
