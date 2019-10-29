package com.yangdb.fuse.asg.strategy.propertyGrouping;

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
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.model.query.quant.HQuant;
import javaslang.collection.Stream;

import java.util.List;

/**
 * Created by benishue on 19-Apr-17.
 */
public class HQuantPropertiesGroupingAsgStrategy implements AsgStrategy {
    // Horizontal Quantifier with Bs below
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, HQuant.class).forEach(hQuant -> {
            List<AsgEBase<RelProp>> relPropsAsgBChildren = AsgQueryUtil.bAdjacentDescendants(hQuant, RelProp.class);

            RelPropGroup rPropGroup;
            List<RelProp> relProps = Stream.ofAll(relPropsAsgBChildren).map(AsgEBase::geteBase).toJavaList();

            if (relProps.size() > 0) {
                rPropGroup = new RelPropGroup(relProps);
                rPropGroup.seteNum(Stream.ofAll(relProps).map(RelProp::geteNum).min().get());

                relPropsAsgBChildren.forEach(hQuant::removeBChild);
            } else {
                rPropGroup = new RelPropGroup();
                rPropGroup.seteNum(Stream.ofAll(AsgQueryUtil.eNums(query)).max().get() + 1);
            }

            hQuant.addBChild(new AsgEBase<>(rPropGroup));
        });
    }
    //endregion
}
