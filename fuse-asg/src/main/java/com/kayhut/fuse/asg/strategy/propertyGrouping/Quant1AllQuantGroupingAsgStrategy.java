package com.kayhut.fuse.asg.strategy.propertyGrouping;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Quant1AllQuantGroupingAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AtomicBoolean hasWorkToDo = new AtomicBoolean(true);
        while(hasWorkToDo.get()) {
            hasWorkToDo.set(false);

            AsgQueryUtil.<Quant1>elements(query, Quant1.class).forEach(quant -> {
                if (quant.geteBase().getqType().equals(QuantType.all)) {
                    AsgQueryUtil.<Quant1, Quant1>nextAdjacentDescendants(quant, Quant1.class).forEach(childQuant -> {
                        if (childQuant.geteBase().getqType().equals(QuantType.all)) {
                            hasWorkToDo.set(true);

                            List<AsgEBase<? extends EBase>> nextChildren = childQuant.getNext();
                            nextChildren.forEach(childQuant::removeNextChild);
                            nextChildren.forEach(quant::addNextChild);
                        }
                    });
                }
            });
        }
    }
    //endregion
}
