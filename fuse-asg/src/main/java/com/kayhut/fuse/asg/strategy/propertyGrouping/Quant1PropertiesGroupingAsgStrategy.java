package com.kayhut.fuse.asg.strategy.propertyGrouping;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.List;

/**
 * Created by benishue on 19-Apr-17.
 */
public class Quant1PropertiesGroupingAsgStrategy implements AsgStrategy {
    // Vertical AND Quantifier with EProps e.g., Q3-2, Q27-2 on V1
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.<Quant1>elements(query, Quant1.class).forEach(quant -> {
                List<AsgEBase<EProp>> ePropsAsgChildren = AsgQueryUtil.nextAdjacentDescendants(quant, EProp.class);
                List<EProp> eProps = Stream.ofAll(ePropsAsgChildren).map(AsgEBase::geteBase).toJavaList();

                if (eProps.size() > 0) {
                    EPropGroup ePropGroup = new EPropGroup(
                            Stream.ofAll(eProps).map(EProp::geteNum).min().get(),
                            quant.geteBase().getqType(),
                            eProps);

                    ePropsAsgChildren.forEach(quant::removeNextChild);
                    quant.addNextChild(new AsgEBase<>(ePropGroup));
                } else {
                    List<AsgEBase<EPropGroup>> ePropsGroupAsgChildren = AsgQueryUtil.nextAdjacentDescendants(quant, EPropGroup.class);
                    if (ePropsGroupAsgChildren.isEmpty()) {
                        EPropGroup ePropGroup = new EPropGroup(Stream.ofAll(AsgQueryUtil.eNums(query)).max().get() + 1);
                        AsgEBase<? extends EBase> ePropGroupAsgEbase = new AsgEBase<>(ePropGroup);
                        quant.addNextChild(ePropGroupAsgEbase);
                    }
                }
            }
        );
    }
    //endregion
}
