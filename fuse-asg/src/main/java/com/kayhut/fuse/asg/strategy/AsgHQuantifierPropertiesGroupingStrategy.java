package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.asg.AsgUtils;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.quant.HQuant;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by benishue on 19-Apr-17.
 */
public class AsgHQuantifierPropertiesGroupingStrategy implements AsgStrategy {
    // Horizontal Quantifier with Bs below
    @Override
    public void apply(AsgQuery query) {
        List<AsgEBase<? extends EBase>> asgBChildrenToBeRemoved = new ArrayList<>();
        List<AsgEBase<? extends EBase>> asgBChildrenToBeAdded = new ArrayList<>();

        AsgQueryUtils.getElements(query, HQuant.class).forEach(hQuant -> {
            for (AsgEBase<? extends EBase> asgEBase : hQuant.getB()) {

                List<AsgEBase<RelProp>> relPropsAsgBChildren =
                        AsgQueryUtils.getBDescendants(
                                asgEBase,
                                (asgEBase1) -> asgEBase1.geteBase().getClass().equals(RelProp.class),
                                (asgEBase1) -> asgEBase1.geteBase().getClass().equals(RelProp.class));

                List<RelProp> relProps = Stream.ofAll(relPropsAsgBChildren).map(asgEBase1 -> asgEBase1.geteBase()).toJavaList();
                if (relProps.size() > 0 ){
                    RelPropGroup rPropGroup = new RelPropGroup();
                    rPropGroup.setrProps(relProps);
                    rPropGroup.seteNum(AsgUtils.getMinEnumFromListOfEBase(relProps));

                    asgBChildrenToBeAdded.add(new AsgEBase<>(rPropGroup));
                }
                asgBChildrenToBeRemoved.addAll(relPropsAsgBChildren);
            };

            asgBChildrenToBeRemoved.forEach(asgEBase -> {
                hQuant.removeBChild(asgEBase);
            }
            );

            asgBChildrenToBeAdded.forEach(asgEBase -> {
                hQuant.addBChild(asgEBase);
            }
            );
        });
    }
    //endregion
}
