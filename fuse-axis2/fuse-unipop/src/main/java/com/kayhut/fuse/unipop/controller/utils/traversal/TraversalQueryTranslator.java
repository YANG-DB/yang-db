package com.kayhut.fuse.unipop.controller.utils.traversal;

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.search.translation.M1QueryTranslator;
import com.kayhut.fuse.unipop.controller.search.translation.PredicateQueryTranslator;
import com.kayhut.fuse.unipop.predicates.ExistsP;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.PropertiesStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;

import java.util.function.Supplier;

/**
 * Created by User on 27/03/2017.
 */
public class TraversalQueryTranslator extends TraversalVisitor<Boolean>{
    //region Constructor
    public TraversalQueryTranslator(
            QueryBuilder queryBuilder,
            boolean shouldCache) {
        this.queryBuilder = queryBuilder;
        this.sequenceSupplier = () -> this.sequenceNumber++;
        this.shouldCache = shouldCache;
    }
    //endregion

    //Override Methods
    @Override
    protected Boolean visitRecursive(Object o) {
        this.queryBuilder.push();
        super.visitRecursive(o);
        this.queryBuilder.pop();
        return Boolean.TRUE;
    }

    @Override
    protected Boolean visitNotStep(NotStep<?> notStep) {
        int nextSequenceNumber = sequenceSupplier.get();
        String currentLabel = "mustNot_" + nextSequenceNumber;
        queryBuilder.bool().mustNot(currentLabel);

        super.visitNotStep(notStep);
        return Boolean.TRUE;
    }

    @Override
    protected Boolean visitOrStep(OrStep<?> orStep) {
        int nextSequenceNumber = sequenceSupplier.get();
        String currentLabel = "should_" + nextSequenceNumber;
        queryBuilder.bool().should(currentLabel);

        super.visitOrStep(orStep);
        return Boolean.TRUE;
    }

    @Override
    protected Boolean visitAndStep(AndStep<?> andStep) {
        int nextSequenceNumber = sequenceSupplier.get();
        String currentLabel = "must_" + nextSequenceNumber;
        queryBuilder.bool().must(currentLabel);

        super.visitAndStep(andStep);
        return Boolean.TRUE;
    }

    @Override
    protected Boolean visitHasStep(HasStep<?> hasStep) {
        PredicateQueryTranslator queryTranslator = new M1QueryTranslator();

        if (hasStep.getHasContainers().size() == 1) {
            queryBuilder = queryTranslator.translate(queryBuilder,
                    hasStep.getHasContainers().get(0).getKey(),
                    hasStep.getHasContainers().get(0).getPredicate());
        } else {
            int nextSequenceNumber = sequenceSupplier.get();
            String currentLabel = "must_" + nextSequenceNumber;
            queryBuilder.bool().must(currentLabel);

            hasStep.getHasContainers().forEach(hasContainer -> {
                queryBuilder.seek(currentLabel);
                queryBuilder = queryTranslator.translate(queryBuilder, hasContainer.getKey(), hasContainer.getPredicate());
            });
        }

        return Boolean.TRUE;
    }

    @Override
    protected Boolean visitTraversalFilterStep(TraversalFilterStep<?> traversalFilterStep) {
        if (traversalFilterStep.getLocalChildren().size() == 1) {
            Traversal.Admin subTraversal = traversalFilterStep.getLocalChildren().get(0);
            if (subTraversal.getSteps().size() == 1
                    && PropertiesStep.class.isAssignableFrom(subTraversal.getSteps().get(0).getClass())) {
                PropertiesStep propertiesStep = (PropertiesStep) subTraversal.getSteps().get(0);

                if (propertiesStep.getPropertyKeys().length == 1) {
                    this.visitRecursive(new HasStep<>(null, new HasContainer(propertiesStep.getPropertyKeys()[0], new ExistsP<Object>())));
                } else {
                    int nextSequenceNumber = sequenceSupplier.get();
                    String currentLabel = "should_" + nextSequenceNumber;
                    queryBuilder.bool().should(currentLabel);

                    for (String key : propertiesStep.getPropertyKeys()) {
                        queryBuilder.seek(currentLabel);
                        this.visitRecursive(new HasStep<>(null, new HasContainer(key, new ExistsP<Object>())));
                    }
                }
            }
        }

        return Boolean.TRUE;
    }
    //endregion

    //region Fields
    private QueryBuilder queryBuilder;
    private int sequenceNumber = 0;
    private Supplier<Integer> sequenceSupplier;

    private boolean shouldCache;
    //endregion
}
