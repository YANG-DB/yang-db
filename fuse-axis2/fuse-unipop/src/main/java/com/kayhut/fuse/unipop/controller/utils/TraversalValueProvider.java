package com.kayhut.fuse.unipop.controller.utils;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

import java.util.Set;

/**
 * Created by benishue on 27-Mar-17.
 */
public interface TraversalValueProvider<T> {
    public Set<String> getValue(Traversal traversal);
}
