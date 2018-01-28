package com.kayhut.fuse.unipop.process.traversal.traverser;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.util.AbstractTraverser;

import java.util.Collections;
import java.util.Set;

/**
 * Created by Roman on 1/25/2018.
 */
public class ThinPathTraverser<T> extends AbstractTraverser<T> {
    //region Constructors
    protected ThinPathTraverser() {

    }

    public ThinPathTraverser(T t) {
        super(t);
    }
    //endregion

    //region Abstract Traverser Implementation
    @Override
    public Set<String> getTags() {
        return Collections.emptySet();
    }

    @Override
    public Path path() {
        return this.path;
    }

    @Override
    public <A> A path(String stepLabel) {
        return this.path.get(stepLabel);
    }

    @Override
    public <A> A path(Pop pop, String stepLabel) {
        return this.path.get(pop, stepLabel);
    }

    @Override
    public <A> A sideEffects(String sideEffectKey) throws IllegalArgumentException {
        return null;
    }

    @Override
    public void sideEffects(String sideEffectKey, Object sideEffectValue) throws IllegalArgumentException {

    }
    //endregion

    //region Fields
    private ThinPath path;
    //endregion
}
