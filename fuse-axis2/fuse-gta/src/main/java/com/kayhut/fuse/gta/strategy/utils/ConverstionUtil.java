package com.kayhut.fuse.gta.strategy.utils;

import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.unipop.controller.utils.CollectionUtil;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Direction;

import java.util.List;

/**
 * Created by Roman on 10/05/2017.
 */
public class ConverstionUtil {
    public static <V> P<V> convertConstraint(com.kayhut.fuse.model.query.Constraint constraint){
        List<Object> range = null;

        switch (constraint.getOp()) {
            case eq: return P.eq(cast(constraint.getExpr()));
            case ne: return P.neq(cast(constraint.getExpr()));
            case gt: return P.gt(cast(constraint.getExpr()));
            case lt: return P.lt(cast(constraint.getExpr()));
            case ge: return P.gte(cast(constraint.getExpr()));
            case le: return P.lte(cast(constraint.getExpr()));
            case inRange:
                range = CollectionUtil.listFromObjectValue(constraint.getExpr());
                return P.between(cast(range.get(0)), cast(range.get(1)));
            case notInRange:
                range = CollectionUtil.listFromObjectValue(constraint.getExpr());
                return P.outside(cast(range.get(0)), cast(range.get(1)));
            case inSet: return P.within(CollectionUtil.listFromObjectValue(constraint.getExpr()));
            case notInSet: return P.without(CollectionUtil.listFromObjectValue(constraint.getExpr()));
            default: throw new RuntimeException("not supported");
        }
    }

    public static Direction convertDirection(Rel.Direction dir) {
        switch (dir) {
            case R:
                return Direction.OUT;
            case L:
                return Direction.IN;
            default:
                throw new IllegalArgumentException("Not Supported Relation Direction: " + dir);
        }
    }

    public static <TIn, TOut> TOut cast(TIn value) {
        return (TOut)value;
    }
}
