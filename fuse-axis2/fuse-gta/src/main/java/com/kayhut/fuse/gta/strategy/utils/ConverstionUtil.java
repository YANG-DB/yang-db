package com.kayhut.fuse.gta.strategy.utils;

import com.kayhut.fuse.model.query.Rel;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.Direction;

/**
 * Created by Roman on 10/05/2017.
 */
public class ConverstionUtil {
    public static P convertConstraint(com.kayhut.fuse.model.query.Constraint constraint){
        switch (constraint.getOp()) {
            case eq: return P.eq(constraint.getExpr());
            case ne: return P.neq(constraint.getExpr());
            case gt: return P.gt(constraint.getExpr());
            case lt: return P.lt(constraint.getExpr());
            case ge: return P.gte(constraint.getExpr());
            case le: return P.lte(constraint.getExpr());
            case inRange:
                Object[] range = (Object[])constraint.getExpr();
                return P.between(range[0], range[1]);
            case inSet: return P.within(constraint.getExpr());
            case notInSet: return P.without(constraint.getExpr());
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
}
