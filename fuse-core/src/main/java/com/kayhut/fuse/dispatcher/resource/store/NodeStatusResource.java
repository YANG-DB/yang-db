package com.kayhut.fuse.dispatcher.resource.store;

import java.util.Map;

public interface NodeStatusResource {
    String NODE = "node";

    /**
     *
     * @param node
     * @return
     */
    Map<String, Object> getMetrics(String node);

    Map<String, Object> getMetrics();

    boolean report();
}
