package com.kayhut.fuse.asg.strategy.constraint;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.properties.constraint.ParameterizedConstraint;
import javaslang.collection.Stream;

import java.util.List;

/**
 * This strategy replaces a likeAny constraint with a single value to an equivalent like constraint with a single value
 */
public class RedundantLikeAnyConstraintAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            Stream.ofAll(ePropGroupAsgEBase.geteBase().getProps())
                    .filter(eProp -> eProp.getCon() != null)
                    .filter(prop -> !ParameterizedConstraint.class.isAssignableFrom(prop.getCon().getClass()))
                    .filter(eProp -> eProp.getCon().getOp().equals(ConstraintOp.likeAny))
                    .filter(eProp -> ((List) eProp.getCon().getExpr()).size() == 1)
                    .forEach(eProp -> eProp.setCon(Constraint.of(ConstraintOp.like, ((List) eProp.getCon().getExpr()).get(0))));
        });
    }
    //endregion
}
