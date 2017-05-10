package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import javaslang.collection.Stream;


import java.util.Arrays;
import java.util.Optional;

/**
 * Created by benishue on 19-Apr-17.
 */
public class AsgEntityPropertiesGroupingStrategy implements AsgStrategy {

    /*
    region AsgStrategy Implementation
    The simple case - no Quantifier involved - e.g., Q142 on V1
    The Entity will have only one EProp child
    */

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtils.getElements(query, EEntityBase.class).forEach(entityBase -> {
            EPropGroup ePropGroup = new EPropGroup();
            AsgEBase<? extends EBase> ePropGroupAsgEbase = new AsgEBase<>(ePropGroup);

            Optional<AsgEBase<EProp>> asgEProp = AsgQueryUtils.getNextAdjacentDescendant(entityBase, EProp.class);
            if (asgEProp.isPresent()){
                ePropGroup.seteProps(Arrays.asList(asgEProp.get().geteBase()));
                ePropGroup.seteNum(asgEProp.get().geteNum());
                entityBase.removeNextChild(asgEProp.get());
            } else {
                ePropGroup.seteNum(Stream.ofAll(AsgQueryUtils.getEnums(query)).max().get() + 1);
            }
            entityBase.addNextChild(ePropGroupAsgEbase);
        });
    }
    //endregion
}
