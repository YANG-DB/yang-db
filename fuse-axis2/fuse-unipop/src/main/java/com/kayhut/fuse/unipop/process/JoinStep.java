package com.kayhut.fuse.unipop.process;

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.ByModulating;
import org.apache.tinkerpop.gremlin.process.traversal.step.TraversalParent;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.MapStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.SackStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.process.traversal.util.FastNoSuchElementException;
import org.apache.tinkerpop.gremlin.structure.Element;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Created by Roman on 7/2/2017.
 */
public class JoinStep<S, E extends Element> extends AbstractStep<S, E> implements TraversalParent, ByModulating {
    //region Constructors
    public JoinStep(Traversal.Admin traversal) {
        super(traversal);

        this.iteratorSupplier = this::nestedLoopAlgorithm;
        this.integrateIdsTraversalFunction = (leftTraversal, ids) -> leftTraversal;
    }
    //endregion

    //region AbstractStep Implementation
    @Override
    protected Traverser.Admin<E> processNextStart() throws NoSuchElementException {
        if (iterator == null) {
            iterator = iteratorSupplier.get();
        }

        return iterator.next();
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
        // THIS IS NOT A BUG!!!
        // DO NOT return the leftTraversal!!!
        // any traversal returned here will undergo strategy application, which will result in a locked traversal.
        // since the left traversal supports integration of ids of the right traversal, it must not be locked when the
        // parent traversal is first activated, thus we 'postpone' strategy application up until after ids integration
        return Collections.singletonList(rightTraversal);
    }

    @Override
    public void addLocalChild(final Traversal.Admin<?, ?> localChildTraversal) {
        if (this.leftTraversal == null) {
            this.leftTraversal = this.integrateChild(localChildTraversal);
            return;
        }

        if (this.rightTraversal == null) {
            this.rightTraversal = this.integrateChild(localChildTraversal);
            return;
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

    //region Properties
    public Traversal.Admin<S, E> getLeftTraversal() {
        return leftTraversal;
    }

    public Traversal.Admin<S, E> getRightTraversal() {
        return rightTraversal;
    }

    public void setIntegrateIdsTraversalFunction(BiFunction<Traversal.Admin<S, E>, Set<Object>, Traversal.Admin<S, E>> integrateIdsTraversalFunction) {
        this.integrateIdsTraversalFunction = integrateIdsTraversalFunction;
    }
    //endregion

    //region Private Methods
    private Iterator<Traverser.Admin<E>> nestedLoopAlgorithm() {
        if (this.leftTraversal == null || this.rightTraversal == null) {
            return Collections.emptyIterator();
        }

        Map<Object, List<Traverser<E>>> rightSet = new HashMap<>();
        while(true) {
            try {
                Traverser<E> elementTraverser = this.rightTraversal.nextTraverser();
                List<Traverser<E>> idTraversers = rightSet.computeIfAbsent(elementTraverser.get().id(), (id) -> new ArrayList<>());
                idTraversers.add(elementTraverser);
            } catch(NoSuchElementException ex){
                break;
            }

        }

        Traversal.Admin<S, E> integratedWithIdsLeftTraversal = integrateIdsToTraversal(this.leftTraversal, rightSet.keySet());
        Iterator<Traverser.Admin<E>> leftIterator = integratedWithIdsLeftTraversal.getEndStep();

        return Stream.ofAll(() -> leftIterator)
                .flatMap(leftTraverser -> mergePaths(leftTraverser, rightSet.get(leftTraverser.get().id())))
                .map(Traverser::asAdmin)
                .iterator();
    }

    private Traversal.Admin<S, E> integrateIdsToTraversal(Traversal.Admin<S, E> traversal, Set<Object> ids) {
        Traversal.Admin<S, E> integratedWithIdsTraversal = integrateIdsTraversalFunction.apply(traversal.clone(), ids);
        integratedWithIdsTraversal = integratedWithIdsTraversal == null ? traversal : integratedWithIdsTraversal;

        if (!integratedWithIdsTraversal.isLocked()) {
            integratedWithIdsTraversal.applyStrategies();
        }

        return integratedWithIdsTraversal;
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

        // ignore the last head of the right traversal as it should be identical to the head of the left traversal
        for(int i = pathObjects.size() - 2; i >= 0 ; i--) {
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

    private Iterator<Traverser.Admin<E>> iterator;
    private Supplier<Iterator<Traverser.Admin<E>>> iteratorSupplier;

    private BiFunction<Traversal.Admin<S, E>, Set<Object>, Traversal.Admin<S, E>> integrateIdsTraversalFunction;

    // the dummy steps are necessary for setting the proper labels for the objects in the new path generated from the split
    // passing 'this' to the split method is not recommended as the join step might have its own labels that we would like to use for the final traverser.
    // it is a hack neccessary due to the tinkerpop step api not providing an alternative beside passing a Step for the split method.
    // another approach would be using our own type of Traverser with our own type of path, but at this time that would be an overkill
    private MapStep<E, Object> dummyStep1 = new SackStep<>(__.start().asAdmin());
    private MapStep<Object, Object> dummyStep2 = new SackStep<>(__.start().asAdmin());
    private MapStep<Object, E> dummyStep3 = new SackStep<>(__.start().asAdmin());
    //endregion
}
