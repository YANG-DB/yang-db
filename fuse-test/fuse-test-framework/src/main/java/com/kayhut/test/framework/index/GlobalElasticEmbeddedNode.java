package com.kayhut.test.framework.index;

import com.kayhut.test.framework.index.ElasticEmbeddedNode;

/**
 * Created by roman.margolis on 01/01/2018.
 */
public class GlobalElasticEmbeddedNode {
    public static ElasticEmbeddedNode getInstance() throws Exception {
        if (instance == null) {
            //instance = new ElasticEmbeddedNode("target/es", 10200, 10300, "fuse.test_elastic");
            instance = new ElasticEmbeddedNode();
        }

        return instance;
    }

    private static ElasticEmbeddedNode instance;
}
