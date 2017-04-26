package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.asg.AsgUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by benishue on 19-Apr-17.
 */
public class AsgRelPropertiesGroupingStrategy implements AsgStrategy {
    // Rel with RelProps e.g., Q190, Q10 on V1
    @Override
    public void apply(AsgQuery query) {
        Map<Integer, AsgEBase> asgRels = AsgUtils.searchForAllEntitiesOfType(query.getStart(), Rel.class);
        asgRels.forEach((eNum,asgRel) -> {
            List<AsgEBase<? extends EBase>> ePropsBChildren = AsgUtils.getEPropsBelowChildren(asgRel);
            if (ePropsBChildren.size() > 0 ){
                AsgEBase<? extends EBase> ePropAsgEBase = ePropsBChildren.get(0);
                EProp eProp = (EProp)ePropAsgEBase.geteBase();

                EPropGroup ePropGroup = new EPropGroup();
                AsgEBase<? extends EBase> ePropGroupAsgEbase = new AsgEBase<>(ePropGroup);

                ePropGroup.seteProps(Arrays.asList(eProp));
                ePropGroup.seteNum(eProp.geteNum());
                //Replacing the entityBase with this "new Entity Wrapper"
                asgRel.addNextChild(ePropGroupAsgEbase);
                asgRel.removeNextChild(ePropAsgEBase);
            }
        });
    }
    //endregion
}
