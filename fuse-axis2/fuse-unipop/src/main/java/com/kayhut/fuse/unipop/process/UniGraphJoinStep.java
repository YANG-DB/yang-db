package com.kayhut.fuse.unipop.process;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.ByModulating;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.structure.Element;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by Roman on 7/2/2017.
 */
public class UniGraphJoinStep<S, E> extends AbstractStep<S, E> implements TraversalParent, ByModulating {
    //region Constructors
    public UniGraphJoinStep(Traversal.Admin traversal) {
        super(traversal);
    }
    //endregion

    //region AbstractStep Implementation
    @Override
    protected Traverser.Admin<E> processNextStart() throws NoSuchElementException {
        return null;
    }
    //endregion

    //region TraversalParent Implementation
    @Override
    public List<Traversal.Admin<S, E>> getLocalChildren() {
        return Arrays.asList(leftTraversal, rightTraversal);
    }

    @Override
    public void addLocalChild(final Traversal.Admin<?, ?> localChildTraversal) {
        if (this.leftTraversal == null) {
            this.leftTraversal = this.integrateChild(localChildTraversal);
        }

        if (this.rightTraversal == null) {
            this.rightTraversal = this.integrateChild(localChildTraversal);
        }

        throw new IllegalStateException("The left and right traversals for join step have already been set: " + this);
    }
    //endregion

    //region ByModulating Implementation
    @Override
    public void modulateBy(final Traversal.Admin<?, ?> traversal) throws UnsupportedOperationException {
        this.addLocalChild(traversal);
    }
    //endregion

    //region Fields
    private Traversal.Admin<S, E> leftTraversal;
    private Traversal.Admin<S, E> rightTraversal;
    //endregion
}
