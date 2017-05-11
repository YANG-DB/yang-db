package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import javaslang.collection.Stream;

import java.util.List;

/**
 * Created by benishue on 19-Apr-17.
 */
public class AsgRelPropertiesGroupingStrategy implements AsgStrategy {
    // Rel with RelProps e.g., Q190, Q10 on V1
    // AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtils.<Rel>getElements(query, Rel.class).forEach(relAsgEBase -> groupRelProps(query, relAsgEBase));
    }
    //endregion

    //region Private Methods
    private void groupRelProps(AsgQuery query, AsgEBase<Rel> asgEBase) {
        RelPropGroup rPropGroup = new RelPropGroup();
        AsgEBase<? extends EBase> rPropGroupAsgEbase = new AsgEBase<>(rPropGroup);

        List<AsgEBase<RelProp>> relPropsAsgBChildren = AsgQueryUtils.getBDescendants(
                asgEBase,
                (asgEBase1) -> asgEBase1.geteBase().getClass().equals(RelProp.class),
                (asgEBase1) -> asgEBase1.geteBase().getClass().equals(RelProp.class) ||
                        asgEBase1.geteBase().getClass().equals(Rel.class));

        List<RelProp> rProps = Stream.ofAll(relPropsAsgBChildren).map(AsgEBase::geteBase).toJavaList();
        if (rProps.size() > 0 ){
            rPropGroup.setrProps(rProps);
            rPropGroup.seteNum(Stream.ofAll(rProps).map(RelProp::geteNum).min().get());
            relPropsAsgBChildren.forEach(asgEBase::removeBChild);
        } else {
            rPropGroup.seteNum(Stream.ofAll(AsgQueryUtils.getEnums(query)).max().get() + 1);
        }

        asgEBase.addBChild(rPropGroupAsgEbase);
    }
    //endregion
}
