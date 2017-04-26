package com.kayhut.fuse.asg.strategy;

import com.kayhut.fuse.asg.AsgUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.quant.HQuant;

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
        Map<Integer, AsgEBase> hQuants = AsgUtils.searchForAllEntitiesOfType(query.getStart(), HQuant.class);
        List<AsgEBase<? extends EBase>> asgBChildrenToBeRemoved = new ArrayList<>();
        List<AsgEBase<? extends EBase>> asgBChildrenToBeAdded = new ArrayList<>();

        hQuants.forEach((eNum,hQuant) -> {
            for (Object o : hQuant.getB()) {
                AsgEBase asgEBase = (AsgEBase) o;
                List<AsgEBase<? extends EBase>> relPropsAsgBChildren = AsgUtils.getRelPropsBelowChildren(asgEBase);

                //If the Parent (i.e the asgEbase parameter) is Of Type RelProp - we will want to combine it
                if ((asgEBase).geteBase() instanceof RelProp){
                    relPropsAsgBChildren.add(asgEBase);
                }

                RelPropGroup rPropGroup = new RelPropGroup();
                if (relPropsAsgBChildren.size() > 0 ){
                    List<RelProp> rProps = relPropsAsgBChildren.stream().map(asgEBase1 -> (RelProp)asgEBase1.geteBase()).collect(Collectors.toList());
                    rPropGroup.setrProps(rProps);
                    rPropGroup.seteNum(AsgUtils.getMinEnumFromListOfEBase(rProps));
                    AsgEBase<? extends EBase> rPropGroupAsgEbase = new AsgEBase<>(rPropGroup);
                    asgBChildrenToBeAdded.add(rPropGroupAsgEbase);
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
