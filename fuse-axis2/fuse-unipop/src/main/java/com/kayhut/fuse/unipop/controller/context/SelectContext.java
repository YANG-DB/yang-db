package com.kayhut.fuse.unipop.controller.context;

import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;

/**
 * Created by Roman on 24/05/2017.
 */
public interface SelectContext {
    Iterable<HasContainer> getSelectPHasContainers();
}
