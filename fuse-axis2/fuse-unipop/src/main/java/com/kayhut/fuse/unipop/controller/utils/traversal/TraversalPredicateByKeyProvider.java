package com.kayhut.fuse.unipop.controller.utils.traversal;

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Elad on 5/23/2017.
 */
public class TraversalPredicateByKeyProvider {

    public Set<P> getPredicateByKey(Traversal traversal, String key) {
        TraversalPredicateByKeyProvider.Visitor visitor = new TraversalPredicateByKeyProvider.Visitor(key);
        visitor.visit(traversal);
        return visitor.getValues();
    }

    //region Visitor
    private class Visitor extends TraversalVisitor<Boolean> {
        //region Constructor
        public Visitor(String key) {
            this.values = new HashSet<>();
            this.key = key;
        }
        //endregion

        //region Override Methods
        @Override
        protected Boolean visitHasStep(HasStep<?> hasStep) {
            Stream.ofAll(hasStep.getHasContainers()).flatMap(hasContainer ->
            {
                if (this.key.equals(hasContainer.getKey()))
                {
                    return Stream.of(hasContainer.getPredicate());
                }
                else
                    return Stream.empty();
            }).forEach(value -> this.values.add(value));

            return Boolean.TRUE;
        }
        //endregion

        //region Properties
        public Set<P> getValues() {
            return this.values;
        }
        //endregion

        //region Fields
        private Set<P> values;
        private String key;
        //endregion
    }
    //endregion
}
