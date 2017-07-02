package com.kayhut.fuse.unipop.process;

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.ByModulating;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;
import org.apache.tinkerpop.gremlin.structure.Element;

import java.util.*;

/**
 * Created by Roman on 7/2/2017.
 */
public class UniGraphJoinStep<S, E extends Element> extends AbstractStep<S, E> implements TraversalParent, ByModulating {
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

    @Override
    public void reset() {
        this.leftTraversal.reset();
        this.rightTraversal.reset();
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

    //region Private Methods
    private Iterator<Traverser.Admin<E>> nestedLoopWithoutIdPushdownAlgorithm() {
        Map<Object, List<Traverser<E>>> rightSet = new HashMap<>();
        while(true) {
            try {
                Traverser<E> elementTraverser = this.rightTraversal.nextTraverser();
                List<Traverser<E>> idTraversers = rightSet.computeIfAbsent(elementTraverser.get().id(), (id) -> new ArrayList<>());
                idTraversers.add(elementTraverser);
            } catch (FastNoSuchElementException ex) {
                break;
            }
        }

        return Stream.ofAll(() -> this.leftTraversal.getEndStep())
                .flatMap(leftTraverser -> mergePaths(leftTraverser, rightSet.get(leftTraverser.get().id())))
                .map(Traverser::asAdmin)
                .iterator();
    }

    private Iterable<Traverser<E>> mergePaths(Traverser<E> leftTraversal, List<Traverser<E>> rightTraversers) {
        if (rightTraversers == null || rightTraversers.isEmpty()) {
            return Collections.emptyList();
        }

        //TODO: implement actual merge paths logic
        return Collections.emptyList();
    }
    //endregion

    //region Fields
    private Traversal.Admin<S, E> leftTraversal;
    private Traversal.Admin<S, E> rightTraversal;
    //endregion
}
