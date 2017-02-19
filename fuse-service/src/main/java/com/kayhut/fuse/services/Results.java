package com.kayhut.fuse.services;

import com.kayhut.fuse.model.Result;
import com.kayhut.fuse.model.transport.Response;

/**
 * Created by lior on 19/02/2017.
 */
public interface Results {

    Response get(String id);
}
