package com.kayhut.fuse.asg.strategy.constraint;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.properties.BaseProp;
import com.kayhut.fuse.model.query.properties.BasePropGroup;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.List;

public class RedundantPropGroupAsgStrategy extends ConstraintTransformationAsgStrategyBase  {
    //region ConstraintTransformationAsgStrategyBase Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            simplifyPropGroup(ePropGroupAsgEBase.geteBase());
        });

        AsgQueryUtil.elements(query, RelPropGroup.class).forEach(ePropGroupAsgEBase -> {
            simplifyPropGroup(ePropGroupAsgEBase.geteBase());
        });
    }
    //endregion

    //region Private Methods
    private <S extends BaseProp, T extends BasePropGroup<S, T>> void simplifyPropGroup(BasePropGroup<S, T> propGroup) {
        Stream.ofAll(propGroup.getGroups()).forEach(this::simplifyPropGroup);

        if (propGroup.getGroups().size() + propGroup.getProps().size() <= 1) {
            propGroup.setQuantType(QuantType.all);

            if (!propGroup.getGroups().isEmpty()) {
                T childGroup = propGroup.getGroups().get(0);
                if (childGroup.getGroups().size() + propGroup.getProps().size() <= 1) {
                    propGroup.getGroups().remove(childGroup);
                    propGroup.getGroups().addAll(childGroup.getGroups());
                    propGroup.getProps().addAll(childGroup.getProps());
                }
            }
        }
    }
    //endregion
}
