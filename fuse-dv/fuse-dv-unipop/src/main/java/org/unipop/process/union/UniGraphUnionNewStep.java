package org.unipop.process.union;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class UniGraphUnionNewStep<S, E> extends AbstractStep<S,E> implements TraversalParent {

    //region Constructors
    public UniGraphUnionNewStep(Traversal.Admin traversal, List<Traversal.Admin<S,E>> unionTraversals) {
        super(traversal);
        this.unionTraversals = unionTraversals;
        this.currentTraversalInWork = 0;
    }

    public UniGraphUnionNewStep(Traversal.Admin traversal) {
        super(traversal);
        this.unionTraversals = new ArrayList<>();
        this.currentTraversalInWork = 0;
    }
    //endregion

    //region Step Implementation
    @Override
    public Set<TraverserRequirement> getRequirements() {
        return this.getSelfAndChildRequirements(TraverserRequirement.PATH);
    }

    @Override
    public List<Traversal.Admin<S,E>> getGlobalChildren() {
        return this.unionTraversals;
    }

    @Override
    public void addGlobalChild(Traversal.Admin<?, ?> globalChildTraversal) {
        this.unionTraversals.add((Traversal.Admin<S, E>)globalChildTraversal);

    }
//endregion

    //region UniBulkStep Implementation

    @Override
    protected Traverser.Admin<E> processNextStart() throws NoSuchElementException {
        if(unionTraversals==null)
            throw FastNoSuchElementException.instance();

        Traversal.Admin<?,E> traversalInWork = null;
        if(currentTraversalInWork >= unionTraversals.size())
            throw FastNoSuchElementException.instance();

        traversalInWork = unionTraversals.get(currentTraversalInWork);

        try {
            return traversalInWork.nextTraverser();
        } catch (FastNoSuchElementException e) {
            Traverser.Admin<E> result = null;
            while (result==null) {
                if (++currentTraversalInWork >= unionTraversals.size())
                    throw FastNoSuchElementException.instance();

                try {
                    traversalInWork = unionTraversals.get(currentTraversalInWork);
                    result = traversalInWork.nextTraverser();
                } catch (NoSuchElementException e1) {}
            }
            return result;
        }
    }
    //endregion

    //region Object Implementation
    @Override
    public UniGraphUnionNewStep<S, E> clone() {
        UniGraphUnionNewStep<S, E> clone = (UniGraphUnionNewStep<S, E>) super.clone();
        clone.unionTraversals = new ArrayList<>();
        unionTraversals.stream().map(t->clone.unionTraversals.add(t.clone()));
        return clone;
    }
    //endregion

    //region Fields
    private List<Traversal.Admin<S,E>> unionTraversals;
    private int currentTraversalInWork ;
    //endregion
}
