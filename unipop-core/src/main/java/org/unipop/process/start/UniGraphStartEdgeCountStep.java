package org.unipop.process.start;

import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.AbstractStep;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.unipop.process.predicate.ReceivesPredicatesHolder;
import org.unipop.process.vertex.UniGraphVertexStep;
import org.unipop.query.StepDescriptor;
import org.unipop.query.aggregation.ReduceEdgeQuery;
import org.unipop.query.aggregation.ReduceQuery;
import org.unipop.query.controller.ControllerManager;
import org.unipop.query.predicates.PredicatesHolder;
import org.unipop.query.predicates.PredicatesHolderFactory;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by Roman on 3/14/2018.
 */
public class UniGraphStartEdgeCountStep<S> extends AbstractStep<S, Long> implements ReceivesPredicatesHolder<S, Long> {


    //region Constructors
    public UniGraphStartEdgeCountStep(UniGraphStartStep uniGraphStartStep, UniGraphVertexStep vertexStep, ControllerManager controllerManager) {
        super(uniGraphStartStep.getTraversal());
        this.vertexPredicates = uniGraphStartStep.getPredicates();
        this.direction = vertexStep.getDirection();
        this.predicates = vertexStep.getPredicates();
        this.controllers = controllerManager.getControllers(ReduceQuery.SearchController.class);
        this.stepDescriptor = new StepDescriptor(this);
    }
    //endregion

    //region AbstractStep Implementation
    @Override
    protected Traverser.Admin<Long> processNextStart() throws NoSuchElementException {
        ReduceQuery reduceQuery = new ReduceEdgeQuery(this.predicates, this.stepDescriptor, this.vertexPredicates, this.direction);

        long count = Stream.ofAll(this.controllers)
                .map(controller -> controller.count(reduceQuery))
                .sum().longValue();

        return this.getTraversal().getTraverserGenerator().generate(null, this, 1L).split(count, this);
    }
    //endregion

    //region ReceivePredicateHolder Implementation
    @Override
    public void addPredicate(PredicatesHolder predicatesHolder) {
        this.predicates = PredicatesHolderFactory.and(this.predicates, predicatesHolder);
    }

    @Override
    public PredicatesHolder getPredicates() {
        return predicates;
    }

    @Override
    public void setLimit(int limit) {
        this.limit = limit;
    }
    //endregion


    public PredicatesHolder getVertexPredicates() {
        return vertexPredicates;
    }

    //region Fields
    private List<ReduceQuery.SearchController> controllers;
    private PredicatesHolder predicates = PredicatesHolderFactory.empty();
    private PredicatesHolder vertexPredicates = PredicatesHolderFactory.empty();;
    private int limit;
    private StepDescriptor stepDescriptor;
    private Direction direction;
    //endregion
}
