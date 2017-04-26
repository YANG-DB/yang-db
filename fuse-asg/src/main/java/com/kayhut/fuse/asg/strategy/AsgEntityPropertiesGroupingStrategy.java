package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.asg.AsgUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;


import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    public void apply(AsgQuery query) {
        Map<Integer, AsgEBase> eEntityBases = AsgUtils.searchForAllEntitiesOfType(query.getStart(), EEntityBase.class);
        eEntityBases.forEach((eNum,entityBase) -> {
            List<AsgEBase<? extends EBase>> ePropsChildren = AsgUtils.getEPropsNextChildren(entityBase);
            if (ePropsChildren.size() > 0 ){
                AsgEBase<? extends EBase> ePropAsgEBase = ePropsChildren.get(0);
                EProp eProp = (EProp)ePropAsgEBase.geteBase();

                EPropGroup ePropGroup = new EPropGroup();
                AsgEBase<? extends EBase> ePropGroupAsgEbase = new AsgEBase<>(ePropGroup);

                ePropGroup.seteProps(Arrays.asList(eProp));
                ePropGroup.seteNum(eProp.geteNum());
                //Replacing the entityBase with this "new Entity Wrapper"
                entityBase.addNextChild(ePropGroupAsgEbase);
                entityBase.removeNextChild(ePropAsgEBase);
            }
        });

    }
    //endregion
}
