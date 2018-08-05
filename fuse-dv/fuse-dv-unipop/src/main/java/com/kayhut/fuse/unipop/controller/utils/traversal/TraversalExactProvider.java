package com.kayhut.fuse.unipop.controller.utils.traversal;

import com.kayhut.fuse.unipop.step.BoostingStepWrapper;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;

import java.util.List;

public class TraversalExactProvider implements TraversalValueProvider<Traversal> {
    //region TraversalValueProvider Implementation
    @Override
    public Traversal getValue(Traversal traversal) {
        Traversal clone = traversal.asAdmin().clone();
        new Visitor().visit(clone);
        return clone;
    }
    //endregion

    //region Visitor
    private static class Visitor extends TraversalVisitor<Traversal> {
        //region TraversalVisitor Implementation
        @Override
        protected Traversal visitNotStep(NotStep<?> notStep) {
            notStep.getTraversal().asAdmin().removeStep(notStep);
            return null;
        }

        @Override
        protected Traversal visitHasStep(HasStep<?> hasStep) {
            if (!isExactPredicate( hasStep.getHasContainers().get(0).getPredicate())) {
                hasStep.getTraversal().asAdmin().removeStep(hasStep);
            }

            return null;
        }
        //endregion

        //region Private Methods
        private boolean isExactPredicate(P<?> predicate) {
            if ((predicate.getBiPredicate() instanceof Compare)) {
                Compare compare = (Compare)predicate.getBiPredicate();
                if (compare.equals(Compare.eq)) {
                    return true;
                }
            }

            if ((predicate.getBiPredicate() instanceof Contains)) {
                Contains contains = (Contains)predicate.getBiPredicate();
                if (contains.equals(Contains.within)) {
                    return true;
                }
            }

            return false;
        }
        //endregion
    }
    //endregion
}
