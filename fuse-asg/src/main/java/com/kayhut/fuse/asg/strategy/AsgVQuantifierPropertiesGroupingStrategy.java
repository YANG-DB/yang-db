package com.kayhut.fuse.asg.strategy;

import com.google.common.collect.Ordering;
import com.kayhut.fuse.asg.AsgUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantBase;
import com.kayhut.fuse.model.query.quant.QuantType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by benishue on 19-Apr-17.
 */
public class AsgVQuantifierPropertiesGroupingStrategy implements AsgStrategy {
    // Vertical AND Quantifier with EProps e.g., Q3-2, Q27-2 on V1
    @Override
    public void apply(AsgQuery query) {
        Map<Integer, AsgEBase> quantifiers = AsgUtils.searchForAllEntitiesOfType(query.getStart(), Quant1.class);
        quantifiers.forEach((eNum,quant) -> {
            if (((QuantBase) quant.geteBase()).getqType() == QuantType.all) {
                EPropGroup ePropGroup = new EPropGroup();
                AsgEBase<? extends EBase> ePropGroupAsgEbase = new AsgEBase<>(ePropGroup);

                List<AsgEBase<? extends EBase>> ePropsAsgChildren = AsgUtils.getEPropsNextChildren(quant);
                List<EProp> eProps = ePropsAsgChildren.stream().map(asgEBase -> (EProp)asgEBase.geteBase()).collect(Collectors.toList());

                ePropGroup.seteProps(eProps);
                ePropGroup.seteNum(AsgUtils.getMinEnumFromListOfEBase(eProps));
                quant.addNextChild(ePropGroupAsgEbase);
                ePropsAsgChildren.forEach(asgEBase -> {
                    quant.removeNextChild(asgEBase);
                });
              }
            }
        );
    }


    //endregion
}
