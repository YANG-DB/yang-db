package com.kayhut.fuse.asg.strategy.PropertiesGrouping;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.quant.HQuant;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 19-Apr-17.
 */
public class AsgHQuantifierPropertiesGroupingStrategy implements AsgStrategy {
    // Horizontal Quantifier with Bs below
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, HQuant.class).forEach(hQuant -> {
            for (AsgEBase<? extends EBase> asgEBase : new ArrayList<>(hQuant.getB())) {

                List<AsgEBase<RelProp>> relPropsAsgBChildren =
                        AsgQueryUtil.bDescendants(
                                asgEBase,
                                (asgEBase1) -> asgEBase1.geteBase().getClass().equals(RelProp.class),
                                (asgEBase1) -> asgEBase1.geteBase().getClass().equals(RelProp.class));

                RelPropGroup rPropGroup;
                List<RelProp> relProps = Stream.ofAll(relPropsAsgBChildren).map(AsgEBase::geteBase).toJavaList();

                if (relProps.size() > 0 ){
                    rPropGroup = new RelPropGroup(relProps);
                    rPropGroup.seteNum(Stream.ofAll(relProps).map(RelProp::geteNum).min().get());

                    relPropsAsgBChildren.forEach(hQuant::removeBChild);
                } else {
                    rPropGroup = new RelPropGroup();
                    rPropGroup.seteNum(Stream.ofAll(AsgQueryUtil.eNums(query)).max().get() + 1);
                }

                hQuant.addBChild(new AsgEBase<>(rPropGroup));
            };
        });
    }
    //endregion
}
