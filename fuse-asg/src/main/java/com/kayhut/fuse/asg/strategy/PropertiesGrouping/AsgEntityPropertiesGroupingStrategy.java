package com.kayhut.fuse.asg.strategy.PropertiesGrouping;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.asg.strategy.AsgStrategyContext;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.quant.HQuant;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;


import java.util.ArrayList;
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
                entityBase.addNextChild(ePropGroupAsgEbase);
            } else {
                int maxEnum = Stream.ofAll(AsgQueryUtils.getEnums(query)).max().get();

                if (entityBase.getNext().isEmpty()) {
                    ePropGroup.seteNum(maxEnum + 1);
                    entityBase.addNextChild(ePropGroupAsgEbase);
                } else {
                    Quant1 quant1 = new Quant1();
                    quant1.seteNum(maxEnum + 1);
                    quant1.setqType(QuantType.all);
                    AsgEBase<Quant1> asgQuant1 = new AsgEBase<>(quant1);

                    ePropGroup.seteNum(maxEnum + 2);

                    asgQuant1.addNextChild(ePropGroupAsgEbase);
                    new ArrayList<>(entityBase.getNext()).forEach(nextAsgEbase -> {
                        entityBase.removeNextChild(nextAsgEbase);
                        asgQuant1.addNextChild(nextAsgEbase);
                    });
                    entityBase.addNextChild(asgQuant1);
                }
            }
        });

    }
    //endregion
}
