package com.yangdb.fuse.asg.strategy.constraint;

/*-
 *
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.asg.strategy.AsgStrategy;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import javaslang.collection.Stream;

import java.util.List;

import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.ignorableConstraints;

/**
 * This strategy replaces a likeAny constraint with a single value to an equivalent like constraint with a single value
 */
public class RedundantLikeAnyConstraintAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            Stream.ofAll(ePropGroupAsgEBase.geteBase().getProps())
                    .filter(eProp -> eProp.getCon() != null)
                    .filter(prop -> !ignorableConstraints.contains(prop.getCon().getClass()))
                    .filter(eProp -> eProp.getCon().getOp().equals(ConstraintOp.likeAny))
                    .filter(eProp -> ((List) eProp.getCon().getExpr()).size() == 1)
                    .forEach(eProp -> eProp.setCon(Constraint.of(ConstraintOp.like, ((List) eProp.getCon().getExpr()).get(0))));
        });
    }
    //endregion
}
