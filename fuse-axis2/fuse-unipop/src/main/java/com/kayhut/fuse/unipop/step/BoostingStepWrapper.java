package com.kayhut.fuse.unipop.step;


import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.apache.tinkerpop.gremlin.process.traversal.traverser.TraverserRequirement;
import org.apache.tinkerpop.gremlin.structure.Element;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

public class BoostingStepWrapper<S,E> implements Step<S,E> {

    public BoostingStepWrapper(Step<S, E> innerStep, long boosting) {
        this.innerStep = innerStep;
        this.boosting = boosting;
    }


    public long getBoosting() {
        return boosting;
    }

    public Step<S, E> getInnerStep() {
        return innerStep;
    }

    @Override
    public void addStarts(Iterator<Traverser.Admin<S>> starts) {
        innerStep.addStarts(starts);
    }

    @Override
    public void addStart(Traverser.Admin<S> start) {
        innerStep.addStart(start);
    }

    @Override
    public void setPreviousStep(Step<?, S> step) {
        innerStep.setPreviousStep(step);
    }

    @Override
    public Step<?, S> getPreviousStep() {
        return innerStep.getPreviousStep();
    }

    @Override
    public void setNextStep(Step<E, ?> step) {
        innerStep.setNextStep(step);
    }

    @Override
    public Step<E, ?> getNextStep() {
        return innerStep.getNextStep();
    }

    @Override
    public <A, B> Traversal.Admin<A, B> getTraversal() {
        return innerStep.getTraversal();
    }

    @Override
    public void setTraversal(Traversal.Admin<?, ?> traversal) {
        innerStep.setTraversal(traversal);
    }

    @Override
    public void reset() {
        innerStep.reset();
    }

    @Override
    public Step<S, E> clone() {
        return new BoostingStepWrapper<>(innerStep, boosting);
    }

    @Override
    public Set<String> getLabels() {
        return innerStep.getLabels();
    }

    @Override
    public void addLabel(String label) {
        innerStep.addLabel(label);
    }

    @Override
    public void removeLabel(String label) {
        innerStep.removeLabel(label);
    }

    @Override
    public void setId(String id) {
        innerStep.setId(id);
    }

    @Override
    public String getId() {
        return innerStep.getId();
    }

    @Override
    public boolean hasNext() {
        return innerStep.hasNext();
    }

    @Override
    public Traverser.Admin<E> next() {
        return innerStep.next();
    }

    @Override
    public String toString() {
        return "BoostingStepWrapper{" +
                "innerStep=" + innerStep +
                ", boosting=" + boosting +
                '}';
    }

    private Step<S,E> innerStep;
    private long boosting;

}
