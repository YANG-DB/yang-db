package com.kayhut.fuse.unipop.controller.utils;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

import java.util.Set;

/**
 * Created by benishue on 27-Mar-17.
 */
public interface TraversalValueByKeyProvider<T> {
    public Set<String> getValueByKey(Traversal traversal, String key);
}
