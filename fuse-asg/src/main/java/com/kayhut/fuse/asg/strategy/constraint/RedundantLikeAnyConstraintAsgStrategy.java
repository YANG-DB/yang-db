package com.kayhut.fuse.asg.strategy.constraint;

import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import javaslang.collection.Stream;

import java.util.List;

public class RedundantLikeAnyConstraintAsgStrategy extends ConstraintTransformationAsgStrategyBase  {
    //region ConstraintTransformationAsgStrategyBase Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            Stream.ofAll(ePropGroupAsgEBase.geteBase().getProps())
                    .filter(eProp -> eProp.getCon() != null)
                    .filter(eProp -> eProp.getCon().getOp().equals(ConstraintOp.likeAny))
                    .filter(eProp -> ((List<String>) eProp.getCon().getExpr()).size() == 1)
                    .forEach(eProp -> eProp.setCon(Constraint.of(ConstraintOp.like, ((List<String>) eProp.getCon().getExpr()).get(0))));
        });
    }
    //endregion
}
