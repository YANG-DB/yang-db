package com.kayhut.fuse.unipop.process;

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.ByModulating;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.MapStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.PathStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.SackStep;
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

    private Iterable<Traverser<E>> mergePaths(Traverser<E> leftTraverser, List<Traverser<E>> rightTraversers) {
        if (rightTraversers == null || rightTraversers.isEmpty()) {
            return Collections.emptyList();
        }

        return Stream.ofAll(rightTraversers).map(rightTraverser -> mergePaths(leftTraverser.asAdmin(), rightTraverser.asAdmin()));
    }

    private Traverser<E> mergePaths(Traverser.Admin<E> leftTraverser, Traverser.Admin<E> rightTraverser) {
        List<Object> pathObjects = rightTraverser.path().objects();
        List<Set<String>> pathLabels = rightTraverser.path().labels();

        Traverser.Admin<Object> childTraverser = null;
        for(int i = pathObjects.size() - 1; i >= 0 ; i--) {
            Set<String> objectLabels = pathLabels.get(i);
            if (objectLabels != null && !objectLabels.isEmpty()) {
                childTraverser = childTraverser == null ?
                        leftTraverser.split(pathObjects.get(i), dummyStep1) :
                        childTraverser.split(pathObjects.get(i), dummyStep2);
                childTraverser.addLabels(objectLabels);
            }
        }

        if (childTraverser == null) {
            return leftTraverser;
        }

        return childTraverser.split(leftTraverser.get(), dummyStep3);
    }
    //endregion

    //region Fields
    private Traversal.Admin<S, E> leftTraversal;
    private Traversal.Admin<S, E> rightTraversal;


    // the dummy steps are necessary for setting the proper labels for the objects in the new path generated from the split
    // passing this to the split method is not recommended as the join step might have its own labels that we would like to use for the final traverser.
    // it is a hack neccessary due to the tinkerpop step api not providing an alternative beside passing a Step for the split method.
    // another approach could be used by using our own type of Traverser with our own type of path, but at this time that would be an overkill
    private MapStep<E, Object> dummyStep1 = new SackStep<>(__.start().asAdmin());
    private MapStep<Object, Object> dummyStep2 = new SackStep<>(__.start().asAdmin());
    private MapStep<Object, E> dummyStep3 = new SackStep<>(__.start().asAdmin());
    //endregion
}
