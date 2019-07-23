package com.yangdb.fuse.asg.strategy.propertyGrouping;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yangdb.fuse.asg.strategy.AsgStrategy;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * Created by benishue on 19-Apr-17.
 */
public class EPropGroupingAsgStrategy implements AsgStrategy {
    /*
    region AsgStrategy Implementation
    The simple case - no Quantifier involved - e.g., Q142 on V1
    The Entity will have only one EProp child
    */

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Stream.ofAll(AsgQueryUtil.elements(query, EEntityBase.class))
                .filter(asgEBase -> !AsgQueryUtil.nextAdjacentDescendant(asgEBase, Quant1.class).isPresent())
                .filter(asgEBase -> !AsgQueryUtil.nextAdjacentDescendant(asgEBase, EPropGroup.class).isPresent())
                .forEach(entityBase -> {
                    Optional<AsgEBase<EProp>> asgEProp = AsgQueryUtil.nextAdjacentDescendant(entityBase, EProp.class);
                    if (asgEProp.isPresent()) {
                        EPropGroup ePropGroup = new EPropGroup(Arrays.asList(asgEProp.get().geteBase()));
                        ePropGroup.seteNum(asgEProp.get().geteNum());
                        entityBase.removeNextChild(asgEProp.get());
                        entityBase.addNextChild(new AsgEBase<>(ePropGroup));
                    } else {
                        EPropGroup ePropGroup = new EPropGroup();
                        int maxEnum = Stream.ofAll(AsgQueryUtil.eNums(query)).max().get();

                        if (entityBase.getNext().isEmpty()) {
                            ePropGroup.seteNum(entityBase.geteNum() * 100 + 1);
                            entityBase.addNextChild(new AsgEBase<>(ePropGroup));
                        } else {
                            Quant1 quant1 = new Quant1();
                            quant1.seteNum(maxEnum + 1);
                            quant1.setqType(QuantType.all);
                            AsgEBase<Quant1> asgQuant1 = new AsgEBase<>(quant1);

                            ePropGroup.seteNum(entityBase.geteNum()*100 + 1);

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
