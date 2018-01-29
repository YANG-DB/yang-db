package com.kayhut.fuse.unipop.process.traversal.traverser;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.B_LP_O_P_S_SE_SL_Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.util.AbstractTraverser;

import java.util.Collections;
import java.util.Set;

/**
 * Created by Roman on 1/25/2018.
 */
public class ThinPathTraverser<T> extends AbstractTraverser<T> {
    //region Constructors
    protected ThinPathTraverser() {
        this.firstSplit = true;
    }

    public ThinPathTraverser(T t, Step<T, ?> step, StringOrdinalDictionary stringOrdinalDictionary) {
        super(t);
        this.path = new ThinPath(stringOrdinalDictionary).extend(t, step.getLabels());
        this.firstSplit = true;
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

    @Override
    public <R> Admin<R> split(R r, Step<T, R> step) {
        ThinPathTraverser<R> clone = (ThinPathTraverser)super.split(r, step);

        if (step.getLabels().isEmpty()) {
            clone.path = this.path;
        } else if (this.firstSplit) {
            clone.path = this.path.extend(r, step.getLabels());
            this.firstSplit = false;
        } else {
            clone.path = this.path.clone().extend(r, step.getLabels());
        }

        return clone;
    }

    @Override
    public Admin<T> split() {
        ThinPathTraverser<T> clone = (ThinPathTraverser)super.split();

        if (this.firstSplit) {
            clone.path = this.path;
            this.firstSplit = false;
        } else {
            clone.path = this.path.clone();
        }

        return clone;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + this.path.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return object != null &&
                object instanceof ThinPathTraverser &&
                ((ThinPathTraverser) object).t.equals(this.t) &&
                ((ThinPathTraverser) object).path.equals(this.path);
    }
    //endregion

    //region Fields
    private Path path;
    private boolean firstSplit;
    //endregion
}
