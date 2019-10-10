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
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import javaslang.collection.Stream;

/**
 * search for "like" constraint within a EpropGroup that does not have "*" in it, and replace with "eq"
 */
public class LikeToEqTransformationAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {

        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            transformGroup(ePropGroupAsgEBase.geteBase());
        });
    }
    //endregion

    //region Private Methods
    private void transformGroup(EPropGroup ePropGroup){
        Stream.ofAll(ePropGroup.getProps())
                .filter(prop -> prop.getCon()!=null)
                .filter(prop -> prop.getCon().getOp().equals(ConstraintOp.like) &&
                !prop.getCon().getExpr().toString().contains("*")).forEach(eProp -> eProp.getCon().setOp(ConstraintOp.eq));

        Stream.ofAll(ePropGroup.getGroups()).forEach(this::transformGroup);
    }
    //endregion
}




