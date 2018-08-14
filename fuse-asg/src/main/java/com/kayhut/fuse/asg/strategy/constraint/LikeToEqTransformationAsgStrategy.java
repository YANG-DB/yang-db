package com.kayhut.fuse.asg.strategy.constraint;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import javaslang.collection.Stream;

import java.util.List;

/**
 * search for "like" constraint within a EpropGroup that does not have "*" in it, and replace with "eq"
 */
public class LikeToEqTransformationAsgStrategy implements AsgStrategy {

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {

        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            transformGroup(ePropGroupAsgEBase.geteBase());
        });
    }

    private void transformGroup(EPropGroup ePropGroup){
        Stream.ofAll(ePropGroup.getProps())
                .filter(prop -> prop.getCon()!=null)
                .filter(prop -> prop.getCon().getOp().equals(ConstraintOp.like) &&
                !prop.getCon().getExpr().toString().contains("*")).forEach(eProp -> eProp.getCon().setOp(ConstraintOp.eq));

        Stream.ofAll(ePropGroup.getGroups()).forEach(g -> transformGroup(g));
    }

}




