package com.kayhut.fuse.asg.strategy.propertyGrouping;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.quant.HQuant;
import javaslang.collection.Stream;

import java.util.List;

/**
 * Created by benishue on 19-Apr-17.
 */
public class RelPropGroupingAsgStrategy implements AsgStrategy {
    // Rel with RelProps e.g., Q190, Q10 on V1
    // AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Stream.ofAll(AsgQueryUtil.elements(query, Rel.class))
                .filter(asgEBase -> !AsgQueryUtil.bDescendant(asgEBase, HQuant.class).isPresent())
                .filter(asgEBase -> !AsgQueryUtil.bAdjacentDescendant(asgEBase, RelPropGroup.class).isPresent())
                .forEach(this::groupRelProps);
    }
    //endregion

    //region Private Methods
    private void groupRelProps(AsgEBase<Rel> asgEBase) {
        RelPropGroup rPropGroup;

        List<AsgEBase<RelProp>> relPropsAsgBChildren = AsgQueryUtil.bDescendants(
                asgEBase,
                (asgEBase1) -> asgEBase1.geteBase().getClass().equals(RelProp.class),
                (asgEBase1) -> asgEBase1.geteBase().getClass().equals(RelProp.class) ||
                        asgEBase1.geteBase().getClass().equals(Rel.class));

        List<RelProp> rProps = Stream.ofAll(relPropsAsgBChildren).map(AsgEBase::geteBase).toJavaList();
        if (rProps.size() > 0) {
            rPropGroup = new RelPropGroup(rProps);
            rPropGroup.seteNum(Stream.ofAll(rProps).map(RelProp::geteNum).min().get());
            relPropsAsgBChildren.forEach(asgEBase::removeBChild);
        } else {
            rPropGroup = new RelPropGroup();
            rPropGroup.seteNum(100*asgEBase.geteNum() + 1);
        }
        asgEBase.addBChild(new AsgEBase<>(rPropGroup));
    }
    //endregion
}
