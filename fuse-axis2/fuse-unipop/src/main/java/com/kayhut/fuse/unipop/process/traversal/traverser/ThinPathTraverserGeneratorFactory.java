package com.kayhut.fuse.unipop.process.traversal.traverser;

import org.apache.tinkerpop.gremlin.process.traversal.TraverserGenerator;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserGeneratorFactory;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;

import java.util.Set;

/**
 * Created by Roman on 1/29/2018.
 */
public class ThinPathTraverserGeneratorFactory implements TraverserGeneratorFactory {
    //region TraverserGeneratorFactory Implementation
    @Override
    public TraverserGenerator getTraverserGenerator(Set<TraverserRequirement> set) {
        return new ThinPathTraverserGenerator();
    }
    //endregion
}
