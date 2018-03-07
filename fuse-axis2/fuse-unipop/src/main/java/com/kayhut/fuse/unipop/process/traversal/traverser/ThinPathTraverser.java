package com.kayhut.fuse.unipop.process.traversal.traverser;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.B_LP_O_P_S_SE_SL_Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.B_O_Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.util.AbstractTraverser;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Roman on 1/25/2018.
 */
public class ThinPathTraverser<T> extends B_O_Traverser<T> {
    //region Constructors
    protected ThinPathTraverser() {

    }

    public ThinPathTraverser(T t, Step<T, ?> step, StringOrdinalDictionary stringOrdinalDictionary) {
        super(t, 1L);
        this.path = new ThinPath(stringOrdinalDictionary).extend(t, step.getLabels());
    }
    //endregion

    //region Abstract Traverser Implementation
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
        if (List.class.isAssignableFrom(r.getClass())) {
            int x = 5;
        }

        ThinPathTraverser<R> clone = (ThinPathTraverser)super.split(r, step);

        clone.path = step.getLabels().isEmpty() ? this.path : this.path.clone().extend(r, step.getLabels());
        return clone;
    }

    @Override
    public Admin<T> split() {
        ThinPathTraverser<T> clone = (ThinPathTraverser)super.split();

        clone.path = this.path.clone();
        return clone;
    }

    @Override
    public void addLabels(final Set<String> labels) {
        this.path = this.path.extend(labels);
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
                ((ThinPathTraverser) object).future.equals(this.future) &&
                ((ThinPathTraverser) object).path.equals(this.path);
    }
    //endregion

    //region Fields
    private Path path;
    //endregion
}
