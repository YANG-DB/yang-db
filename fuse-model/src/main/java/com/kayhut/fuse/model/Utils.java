package com.kayhut.fuse.model;

import java.util.UUID;

/**
 * Created by lior on 19/02/2017.
 */
public interface Utils {

    static String getOrCreateId(String id) {
        return id!=null ? id : UUID.randomUUID().toString();
    }
}
