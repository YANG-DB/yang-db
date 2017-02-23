package com.kayhut.fuse.dispatcher;

import com.kayhut.fuse.model.transport.Response;

/**
 * Created by lior on 23/02/2017.
 */
public interface DispatcherDriver<T> {
    Response wrap(T input);
}
