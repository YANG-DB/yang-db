package com.kayhut.fuse.asg.strategy.PropertiesGrouping;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.asg.strategy.AsgStrategyContext;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by benishue on 19-Apr-17.
 */
public class AsgQuant1PropertiesGroupingStrategy implements AsgStrategy {
    // Vertical AND Quantifier with EProps e.g., Q3-2, Q27-2 on V1
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtils.<Quant1>getElements(query, Quant1.class).forEach(quant -> {
            if (quant.geteBase().getqType() == QuantType.all) {

                List<AsgEBase<EProp>> ePropsAsgChildren = AsgQueryUtils.getNextAdjacentDescendants(quant, EProp.class);
                List<EProp> eProps = Stream.ofAll(ePropsAsgChildren).map(AsgEBase::geteBase).toJavaList();

                if (eProps.size() > 0) {
                    EPropGroup ePropGroup = new EPropGroup();
                    AsgEBase<? extends EBase> ePropGroupAsgEbase = new AsgEBase<>(ePropGroup);
                    ePropGroup.seteProps(eProps);
                    ePropGroup.seteNum(Stream.ofAll(eProps).map(EProp::geteNum).min().get());
                    ePropsAsgChildren.forEach(quant::removeNextChild);
                    quant.addNextChild(ePropGroupAsgEbase);
                } else {
                    List<AsgEBase<EPropGroup>> ePropsGroupAsgChildren = AsgQueryUtils.getNextAdjacentDescendants(quant, EPropGroup.class);
                    if (ePropsGroupAsgChildren.isEmpty()) {
                        EPropGroup ePropGroup = new EPropGroup();
                        AsgEBase<? extends EBase> ePropGroupAsgEbase = new AsgEBase<>(ePropGroup);
                        ePropGroup.seteNum(Stream.ofAll(AsgQueryUtils.getEnums(query)).max().get());
                        quant.addNextChild(ePropGroupAsgEbase);
                    }
                }

              }
            }
        );
    }


    //endregion
}
