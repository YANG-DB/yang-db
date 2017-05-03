package com.kayhut.fuse.unipop.controller.utils;

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by benishue on 27-Mar-17.
 */
public class TraversalValuesProvider implements TraversalValueProvider<Set<String>> {
    //region TraversalValueProvider Implementation
    @Override
    public Set<String> getValue(Traversal traversal) {
        Visitor visitor = new Visitor();
        visitor.visit(traversal);
        return visitor.getValues();
    }
    //endregion

    //region Visitor
    private class Visitor extends TraversalVisitor<Boolean> {
        //region Constructor
        public Visitor() {
            this.values = new HashSet<>();
        }
        //endregion

        //region Override Methods
        @Override
        protected Boolean visitHasStep(HasStep hasStep) {
            Stream.of(hasStep.getHasContainers()).flatMap((Object hasContainerObj) -> {
                        HasContainer hasContainer = (HasContainer)hasContainerObj;
                        if (Iterable.class.isAssignableFrom(hasContainer.getValue().getClass())) {
                            return Stream.of(((Iterable)hasContainer.getValue())).map(value -> value.toString());
                        } else if (String[].class.isAssignableFrom(hasContainer.getValue().getClass())) {
                            return Stream.of((String[])hasContainer.getValue());
                        } else {
                            return Stream.of(hasContainer.getValue());
                        }
                    }).forEach(value -> this.values.add(value.toString()));

            return Boolean.TRUE;
        }
        //endregion

        //region Properties
        public Set<String> getValues() {
            return this.values;
        }
        //endregion

        //region Fields
        private Set<String> values;
        //endregion
    }
    //endregion
}
