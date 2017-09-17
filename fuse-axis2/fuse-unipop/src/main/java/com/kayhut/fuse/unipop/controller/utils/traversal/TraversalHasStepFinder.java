package com.kayhut.fuse.unipop.controller.utils.traversal;

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;

import java.util.function.Predicate;

/**
 * Created by Roman on 9/17/2017.
 */
public class TraversalHasStepFinder implements TraversalValueProvider<HasStep> {
    //region Constructors
    public TraversalHasStepFinder(Predicate<HasStep<?>> hasStepPredicate) {
        this.hasStepPredicate = hasStepPredicate;
    }
    //endregion

    //region TraversalValueProvider Implementation
    @Override
    public HasStep getValue(Traversal traversal) {
        Visitor visitor = new Visitor(this.hasStepPredicate);
        visitor.visit(traversal);
        return visitor.getHasStep();
    }
    //endregions

    //region Fields
    private Predicate<HasStep<?>> hasStepPredicate;
    //endregion

    //region Visitor
    private class Visitor extends TraversalVisitor<Boolean> {
        //region Constructors
        public Visitor(Predicate<HasStep<?>> predicate) {
            this.predicate = predicate;
        }
        //endregion

        //region Override Methods
        @Override
        protected Boolean visitHasStep(HasStep<?> hasStep) {
            if (this.hasStep != null) {
                return Boolean.FALSE;
            } else if (this.predicate.test(hasStep)) {
                this.hasStep = hasStep;
                return Boolean.TRUE;
            }

            return Boolean.FALSE;
        }
        //endregion

        //region Properties
        public HasStep getHasStep() {
            return this.hasStep;
        }
        //endregion

        //region Fields
        private Predicate<HasStep<?>> predicate;
        private HasStep hasStep;
        //endregion
    }
    //endregion
}
