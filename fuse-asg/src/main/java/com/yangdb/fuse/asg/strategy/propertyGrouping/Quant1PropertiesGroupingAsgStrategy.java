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
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by benishue on 19-Apr-17.
 */
public class Quant1PropertiesGroupingAsgStrategy implements AsgStrategy {
    // Vertical AND Quantifier with EProps e.g., Q3-2, Q27-2 on V1
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        // phase 1 - group all Eprops to EPropGroups
        AsgQueryUtil.elements(query, Quant1.class).forEach(quant -> {
                List<AsgEBase<EProp>> ePropsAsgChildren = AsgQueryUtil.nextAdjacentDescendants(quant, EProp.class);
                List<EProp> eProps = Stream.ofAll(ePropsAsgChildren).map(AsgEBase::geteBase).toJavaList();

                if (!eProps.isEmpty()) {
                    EPropGroup ePropGroup = new EPropGroup(
                            Stream.ofAll(eProps).map(EProp::geteNum).min().get(),
                            quant.geteBase().getqType(),
                            eProps);

                    ePropsAsgChildren.forEach(quant::removeNextChild);
                    quant.addNextChild(new AsgEBase<>(ePropGroup));
                } else {
                    List<AsgEBase<EPropGroup>> ePropsGroupAsgChildren = AsgQueryUtil.nextAdjacentDescendants(quant, EPropGroup.class);
                    Optional<AsgEBase<EBase>> ancestor = AsgQueryUtil.ancestor(quant, asgEBase -> asgEBase.geteBase() instanceof ETyped,
                            asgEBase -> !(asgEBase.geteBase() instanceof Rel));
                    if (ePropsGroupAsgChildren.isEmpty()
                            && ancestor.isPresent()
                            && quant.geteBase().getqType().equals(QuantType.all)) {
                        EPropGroup ePropGroup = new EPropGroup(Stream.ofAll(AsgQueryUtil.eNums(query)).max().get() + 1);
                        AsgEBase<? extends EBase> ePropGroupAsgEbase = new AsgEBase<>(ePropGroup);
                        quant.addNextChild(ePropGroupAsgEbase);
                    }
                }
            }
        );

        // phase 2 - group all EpropGroups to other EpropGroups
        AsgQueryUtil.elements(query, Quant1.class).forEach(this::groupEpropGroups);
    }
    //endregion

    //region Private Methods
    private void groupEpropGroups(AsgEBase<Quant1> quant1AsgEBase) {
        AsgQueryUtil.<Quant1, Quant1>nextAdjacentDescendants(quant1AsgEBase, Quant1.class)
                .forEach(this::groupEpropGroups);

        List<AsgEBase<EPropGroup>> epropGroups = AsgQueryUtil.nextAdjacentDescendants(quant1AsgEBase, EPropGroup.class);
        //if (quant1AsgEBase.getNext().size() == epropGroups.size()) {
            if (epropGroups.size() > 1) {
                EPropGroup groupedEPropGroup = new EPropGroup(
                        Stream.ofAll(epropGroups).map(AsgEBase::geteNum).min().get(),
                        quant1AsgEBase.geteBase().getqType(),
                        Collections.emptyList(),
                        Stream.ofAll(epropGroups)
                                .filter(asgEBase -> asgEBase.geteBase().getProps().size() > 0 || asgEBase.geteBase().getGroups().size() > 0)
                                .map(AsgEBase::geteBase).toJavaList());

                epropGroups.forEach(quant1AsgEBase::removeNextChild);
                quant1AsgEBase.addNextChild(AsgEBase.Builder.get().withEBase(groupedEPropGroup).build());

                List<EProp> projectionProps = groupedEPropGroup.findAll(eProp -> eProp.getProj() != null);
                groupedEPropGroup.consumeAll(eProp -> eProp.getProj() != null, (group, eprop) -> group.getProps().remove(eprop));
                groupedEPropGroup.getProps().addAll(projectionProps);
            }
        //}

        Optional<AsgEBase<Quant1>> parentQuant = AsgQueryUtil.adjacentAncestor(quant1AsgEBase, Quant1.class);
        if (parentQuant.isPresent() && quant1AsgEBase.getNext().size() == 1) {
            AsgEBase<? extends EBase> child = quant1AsgEBase.getNext().get(0);
            parentQuant.get().removeNextChild(quant1AsgEBase);
            quant1AsgEBase.removeNextChild(child);
            parentQuant.get().addNextChild(child);
        }
    }
    //endregion
}
