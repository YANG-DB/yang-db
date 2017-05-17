package com.kayhut.fuse.asg.strategy.PropertiesGrouping;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.asg.strategy.AsgStrategyContext;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
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
            EPropGroup ePropGroup;
            Optional<AsgEBase<EProp>> asgEProp = AsgQueryUtils.getNextAdjacentDescendant(entityBase, EProp.class);
            if (asgEProp.isPresent()){
                ePropGroup = new EPropGroup(Arrays.asList(asgEProp.get().geteBase()));
                ePropGroup.seteNum(asgEProp.get().geteNum());
                entityBase.removeNextChild(asgEProp.get());
                entityBase.addNextChild(new AsgEBase<>(ePropGroup));
            } else {
                int maxEnum = Stream.ofAll(AsgQueryUtils.getEnums(query)).max().get();
                if (entityBase.getNext().isEmpty()) {
                    ePropGroup = new EPropGroup();
                    ePropGroup.seteNum(maxEnum + 1);
                    entityBase.addNextChild(new AsgEBase<>(ePropGroup));
                } else {
                    ePropGroup = new EPropGroup();
                    Quant1 quant1 = new Quant1();
                    quant1.seteNum(maxEnum + 1);
                    quant1.setqType(QuantType.all);
                    AsgEBase<Quant1> asgQuant1 = new AsgEBase<>(quant1);

                    ePropGroup.seteNum(maxEnum + 2);
                    asgQuant1.addNextChild(new AsgEBase<>(ePropGroup));
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
