package com.kayhut.fuse.unipop.process;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
                        leftTraverser.split(pathObjects.get(i), LabelsStep.of(objectLabels)) :
                        childTraverser.split(pathObjects.get(i), LabelsStep.of(objectLabels));
            }
        }

        if (childTraverser == null) {
            return leftTraverser;
        }

        return childTraverser.split(leftTraverser.get(), LabelsStep.of());
    }
    //endregion

    //region Fields
    private Traversal.Admin<S, E> leftTraversal;
    private Traversal.Admin<S, E> rightTraversal;

    private Iterator<Traverser.Admin<E>> iterator;
    private Supplier<Iterator<Traverser.Admin<E>>> iteratorSupplier;

    private BiFunction<Traversal.Admin<S, E>, Set<Object>, Traversal.Admin<S, E>> integrateIdsTraversalFunction;
    //endregion

    //region LabelsStep
    private static class LabelsStep<S, E> extends AbstractStep<S, E> {
        //region Static
        public static <S, E> LabelsStep<S, E> of(String...labels) {
            return new LabelsStep<>(labels);
        }

        public static <S, E> LabelsStep<S, E> of(Iterable<String> labels) {
            return new LabelsStep<>(labels);
        }
        //endregion

        //region Constructors
        public LabelsStep(String...labels) {
            this(Stream.of(labels));
        }

        public LabelsStep(Iterable<String> labels) {
            super(null);
            this.labels.addAll(Stream.ofAll(labels).toJavaList());
        }
        //endregion

        @Override
        protected Traverser.Admin<E> processNextStart() throws NoSuchElementException {
            return null;
        }
    }
    //endregion
}
