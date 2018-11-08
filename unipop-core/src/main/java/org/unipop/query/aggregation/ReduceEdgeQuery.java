package org.unipop.query.aggregation;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.unipop.query.StepDescriptor;
import org.unipop.query.predicates.PredicatesHolder;

public class ReduceEdgeQuery extends ReduceQuery {
    public ReduceEdgeQuery(PredicatesHolder predicates, StepDescriptor stepDescriptor, PredicatesHolder vertexPredicates, Direction direction) {
        super(predicates, stepDescriptor);
        this.vertexPredicates = vertexPredicates;
        this.direction = direction;
    }

    public PredicatesHolder getVertexPredicates() {
        return vertexPredicates;
    }

    public Direction getDirection() {
        return direction;
    }

    private PredicatesHolder vertexPredicates;
    private Direction direction;
}
