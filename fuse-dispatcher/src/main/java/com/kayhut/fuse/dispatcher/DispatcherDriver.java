package com.kayhut.fuse.dispatcher;

import com.kayhut.fuse.model.transport.ContentResponse;

/**
 * Created by lior on 23/02/2017.
 */
public interface DispatcherDriver<T> {
    ContentResponse wrap(T input);
}
