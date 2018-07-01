package com.kayhut.fuse.unipop.process.traversal.traverser;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.B_LP_O_P_S_SE_SL_Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.B_O_S_SE_SL_Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.B_O_Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.util.AbstractTraverser;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Roman on 1/25/2018.
 */
public class ThinPathTraverser<T> extends B_O_S_SE_SL_Traverser<T> {
    //region Constructors
    protected ThinPathTraverser() {

    }

    public ThinPathTraverser(T t, Step<T, ?> step, StringOrdinalDictionary stringOrdinalDictionary) {
        super(t, step, 1L);
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
    public <R> Admin<R> split(R r, Step<T, R> step) {
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
        return object instanceof B_O_S_SE_SL_Traverser
                && ((ThinPathTraverser) object).t.equals(this.t)
                && ((ThinPathTraverser) object).future.equals(this.future)
                && ((ThinPathTraverser) object).loops == this.loops
                && ((ThinPathTraverser) object).path.equals(this.path)
                && (null == this.sack || (null != this.sideEffects && null != this.sideEffects.getSackMerger()));
    }
    //endregion

    //region Fields
    private Path path;
    //endregion
}
