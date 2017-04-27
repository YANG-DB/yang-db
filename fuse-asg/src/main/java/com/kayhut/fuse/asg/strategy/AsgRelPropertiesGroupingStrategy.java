package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.asg.AsgUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by benishue on 19-Apr-17.
 */
public class AsgRelPropertiesGroupingStrategy implements AsgStrategy {
    // Rel with RelProps e.g., Q190, Q10 on V1
    @Override
    public void apply(AsgQuery query) {
        Map<Integer, AsgEBase> asgRels = AsgUtils.searchForAllEntitiesOfType(query.getStart(), Rel.class);
        asgRels.forEach((eNum,asgRel) -> {
            groupRelProps(asgRel);
        });
    }

    private void groupRelProps(AsgEBase asgEBase) {
        List<AsgEBase<? extends EBase>> relPropsAsgBChildren = AsgUtils.getRelPropsBelowChildren(asgEBase);

        RelPropGroup rPropGroup = new RelPropGroup();
        AsgEBase<? extends EBase> rPropGroupAsgEbase = new AsgEBase<>(rPropGroup);
        if (relPropsAsgBChildren.size() > 0 ){
            List<RelProp> rProps = relPropsAsgBChildren.stream().map(asgEBase1 -> (RelProp)asgEBase1.geteBase()).collect(Collectors.toList());
            rPropGroup.setrProps(rProps);
            rPropGroup.seteNum(AsgUtils.getMinEnumFromListOfEBase(rProps));
            asgEBase.addBChild(rPropGroupAsgEbase);
            relPropsAsgBChildren.forEach(asgEBase2 -> {
                asgEBase.removeBChild(asgEBase2);
            });
        }
    }
    //endregion
}
