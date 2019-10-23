package com.yangdb.fuse.asg.strategy.constraint;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.query.properties.BaseProp;
import com.yangdb.fuse.model.query.properties.BasePropGroup;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.model.query.quant.QuantType;
import javaslang.collection.Stream;

/**
 * This strategy simplifies property groups by building equivalent less complex, less nested groups.
 * The strategy searches recursively for 'simple' groups, where a 'simple' group is defined to be a group that has either
 * a single property or a single child group. Any 'simple' group is changed by setting it's quant type to be 'All'
 * (because 'All' and 'Some' are equivalent in a group where there's only 1 property or child group, and 'All' is preferable
 * to 'Some' because currently 'Some' quants will cost more in other aspects of the fuse engine.
 *
 * Further more, if a group is simple and it has a child group, the child groups props and groups are added to the current group
 * the groups quant type is set to be the child groups quant type and the child group itself is discarded.
 * This phase makes nested group structures less complex when they are redundant.
 */
public class RedundantPropGroupAsgStrategy implements AsgStrategy {
    //region ConstraintTransformationAsgStrategyBase Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            simplifyPropGroup(ePropGroupAsgEBase.geteBase());
        });

        AsgQueryUtil.elements(query, RelPropGroup.class).forEach(ePropGroupAsgEBase -> {
            simplifyPropGroup(ePropGroupAsgEBase.geteBase());
        });
    }
    //endregion

    //region Private Methods
    private <S extends BaseProp, T extends BasePropGroup<S, T>> void simplifyPropGroup(BasePropGroup<S, T> propGroup) {
        Stream.ofAll(propGroup.getGroups()).forEach(this::simplifyPropGroup);

        if (isSimplePropGroup(propGroup)) {
            propGroup.setQuantType(QuantType.all);

            if (!propGroup.getGroups().isEmpty()) {
                T childGroup = propGroup.getGroups().get(0);
                /*if (isSimplePropGroup(childGroup)) {
                    propGroup.getGroups().remove(childGroup);
                    propGroup.getGroups().addAll(childGroup.getGroups());
                    propGroup.getProps().addAll(childGroup.getProps());
                }*/

                propGroup.getGroups().remove(childGroup);

                propGroup.getGroups().addAll(childGroup.getGroups());
                propGroup.getProps().addAll(childGroup.getProps());
                propGroup.setQuantType(childGroup.getQuantType());
            }
        }
    }

    private <S extends BaseProp, T extends BasePropGroup<S, T>> boolean isSimplePropGroup(BasePropGroup<S, T> propGroup) {
        return propGroup.getGroups().size() + propGroup.getProps().size() <= 1;
    }
    //endregion
}
