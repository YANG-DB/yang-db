package com.kayhut.fuse.unipop.controller.utils.traversal;

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.*;

/**
 * Created by Roman on 15/05/2017.
 */
public class TraversalToStringProvider implements TraversalValueProvider<String> {
    //region TraversalIdProvider
    @Override
    public String getValue(Traversal traversal) {
        Visitor visitor = new Visitor();
        visitor.visit(traversal);
        return visitor.getBuilder().toString();
    }
    //endregion

    private class Visitor extends TraversalVisitor<Boolean> {
        //region Constructor
        public Visitor() {
            this.builder = new StringBuilder();
        }
        //endregion

        //region Override Methods
        @Override
        protected Boolean visitNotStep(NotStep<?> notStep) {
            this.builder.append("not").append("(");
            notStep.getLocalChildren().forEach(child -> {
                visitRecursive((Traversal) child);
                this.builder.append(", ");
            });
            this.builder.delete(this.builder.length() - 2, this.builder.length()).append(")");
            return Boolean.TRUE;
        }

        protected Boolean visitOrStep(OrStep<?> orStep) {
            this.builder.append("or").append("(");
            orStep.getLocalChildren().forEach(child -> {
                visitRecursive(child);
                this.builder.append(", ");
            });
            this.builder.delete(this.builder.length() - 2, this.builder.length()).append(")");
            return Boolean.TRUE;
        }

        protected Boolean visitAndStep(AndStep<?> andStep) {
            this.builder.append("and").append("(");
            andStep.getLocalChildren().forEach(child -> {
                visitRecursive((Traversal) child);
                this.builder.append(", ");
            });
            this.builder.delete(this.builder.length() - 2, this.builder.length()).append(")");
            return Boolean.TRUE;
        }

        @Override
        protected Boolean visitHasStep(HasStep<?> hasStep) {
            if (hasStep.getHasContainers().size() == 1) {
                Stream.ofAll(hasStep.getHasContainers()).forEach(hasContainer -> this.builder.append(hasContainer.toString()));
            } else {
                this.builder.append("and").append("(");
                Stream.ofAll(hasStep.getHasContainers()).forEach(hasContainer -> this.builder.append(hasContainer.toString()).append(", "));
                this.builder.delete(this.builder.length() - 2, this.builder.length()).append(")");
            }
            return Boolean.TRUE;
        }
        //endregion

        //region Properties
        public StringBuilder getBuilder() {
            return this.builder;
        }
        //endregion

        //region Fields
        private StringBuilder builder;
        //endregion
    }
}
