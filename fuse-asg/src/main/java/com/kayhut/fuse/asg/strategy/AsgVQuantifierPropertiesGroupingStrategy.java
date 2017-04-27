package com.kayhut.fuse.asg.strategy;

import com.google.common.collect.Ordering;
import com.kayhut.fuse.asg.AsgUtils;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantBase;
import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

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
        AsgQueryUtils.<Quant1>getElements(query, Quant1.class).forEach(quant -> {
            if (quant.geteBase().getqType() == QuantType.all) {

                List<AsgEBase<EProp>> ePropsAsgChildren = AsgQueryUtils.getNextAdjacentDescendants(quant,
                        asgEBase -> asgEBase.geteBase().getClass().equals(EProp.class));

                List<EProp> eProps = Stream.ofAll(ePropsAsgChildren).map(AsgEBase::geteBase).toJavaList();

                EPropGroup ePropGroup = new EPropGroup();
                AsgEBase<? extends EBase> ePropGroupAsgEbase = new AsgEBase<>(ePropGroup);
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
