package com.kayhut.fuse.asg.strategy.constraint;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.properties.constraint.ParameterizedConstraint;
import javaslang.collection.Stream;

import java.util.List;

import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.ignorableConstraints;

/**
 * This strategy replaces an inSet constraint with a single value to an equivalent eq constraint with a single value
 */
public class RedundantInSetConstraintAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            cleanRedundantInSetEprops(ePropGroupAsgEBase.geteBase());
        });
    }
    //endregion

    //region Private Methods
    private void cleanRedundantInSetEprops(EPropGroup ePropGroup) {
        Stream.ofAll(ePropGroup.getProps())
                .filter(eProp -> eProp.getCon() != null)
                .filter(prop -> !ignorableConstraints.contains(prop.getCon().getClass()))
                .filter(eProp -> eProp.getCon().getOp().equals(ConstraintOp.inSet))
                .filter(eProp -> ((List) eProp.getCon().getExpr()).size() == 1)
                .forEach(eProp -> eProp.setCon(Constraint.of(ConstraintOp.eq, ((List) eProp.getCon().getExpr()).get(0))));

        Stream.ofAll(ePropGroup.getGroups()).forEach(this::cleanRedundantInSetEprops);
    }
    //endregion
}
