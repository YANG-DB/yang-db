package com.kayhut.fuse.unipop.controller.utils.traversal;

import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.search.translation.M1QueryTranslator;
import com.kayhut.fuse.unipop.controller.search.translation.PredicateQueryTranslator;
import com.kayhut.fuse.unipop.step.BoostingStepWrapper;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Step;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.filter.*;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.PropertiesStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.unipop.process.predicate.ExistsP;

import java.util.List;
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
        String currentBoolLabel = "bool_" + nextSequenceNumber;
        String currentFilterLabel = "filter_" + nextSequenceNumber;
        String currentShouldLabel = "should_" + nextSequenceNumber;
        queryBuilder.bool(currentBoolLabel);

        List<? extends Traversal.Admin<?, ?>> localChildren = orStep.getLocalChildren();
        BoostingTraversalVisitor boostingTraversalVisitor = new BoostingTraversalVisitor();
        Stream<Tuple2<Traversal, Boolean>> isBoostingTraversal = Stream.ofAll(localChildren).map(t -> new Tuple2(t, boostingTraversalVisitor.visit(t)));
        Stream<Traversal> filters = isBoostingTraversal.filter(t -> !t._2).map(t -> t._1);
        Stream<Traversal> shouldFilters = isBoostingTraversal.filter(t -> t._2).map(t -> t._1);

        if(filters.size() > 0) {
            queryBuilder.filter(currentFilterLabel).bool().should();
            filters.forEach(f -> super.visitRecursive(f));
            queryBuilder.seek(currentBoolLabel);
        }
        queryBuilder.should(currentShouldLabel);
        shouldFilters.forEach(f -> super.visitRecursive(f));

        return Boolean.TRUE;
    }

    @Override
    protected Boolean visitAndStep(AndStep<?> andStep) {
        int nextSequenceNumber = sequenceSupplier.get();
        String currentBoolLabel = "bool_" + nextSequenceNumber;
        String currentFilterLabel = "filter_" + nextSequenceNumber;
        String currentMustLabel = "must_" + nextSequenceNumber;

        queryBuilder.bool(currentBoolLabel);
        List<? extends Traversal.Admin<?, ?>> localChildren = andStep.getLocalChildren();
        BoostingTraversalVisitor boostingTraversalVisitor = new BoostingTraversalVisitor();
        Stream<Tuple2<Traversal, Boolean>> isBoostingTraversal = Stream.ofAll(localChildren).map(t -> new Tuple2(t, boostingTraversalVisitor.visit(t)));
        Stream<Traversal> filters = isBoostingTraversal.filter(t -> !t._2).map(t -> t._1);
        Stream<Traversal> mustFilters = isBoostingTraversal.filter(t -> t._2).map(t -> t._1);

        if(filters.size() > 0) {
            queryBuilder.filter(currentFilterLabel).bool().must();
            filters.forEach(f -> super.visitRecursive(f));
            queryBuilder.seek(currentBoolLabel);
        }
        queryBuilder.must(currentMustLabel);
        mustFilters.forEach(f -> super.visitRecursive(f));

        return Boolean.TRUE;
    }

    @Override
    protected Boolean visitHasStep(HasStep<?> hasStep) {
        PredicateQueryTranslator queryTranslator = M1QueryTranslator.instance;

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

    @Override
    protected Boolean visitBoostingStep(BoostingStepWrapper o) {
        queryBuilder.boost(o.getBoosting());
        super.visitBoostingStep(o);
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
