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
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RankingProp;
import com.yangdb.fuse.model.query.properties.ScoreEPropGroup;
import javaslang.collection.Stream;

import java.util.List;

/*
 * propagating ranking properties upwards (wrapped containers with RankingProp Interface)
 */
public class RankingPropertiesPropagationAsgStrategy implements AsgStrategy {

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        //go over all EPropGroups and replace with a scored group in case the group is not scored
        // and contains some descendant which is scored
        Stream.ofAll(AsgQueryUtil.elements(query, EPropGroup.class))
                .forEach(property -> {
                    property.seteBase(replaceRecursive(property.geteBase()));
                });
    }

    private EPropGroup replaceRecursive(EPropGroup group){
        if(group  instanceof RankingProp){
            return group;
        }
        List<EPropGroup> newGroups = Stream.ofAll(group.getGroups()).map(g -> replaceRecursive(g)).toJavaList();
        group.getGroups().clear();
        group.getGroups().addAll(newGroups);

        if(!Stream.ofAll(group.getProps()).find(p -> p instanceof RankingProp).isEmpty()
                || !Stream.ofAll(group.getGroups()).find(g -> g instanceof RankingProp).isEmpty() ){
            return new ScoreEPropGroup(group, 1);
        }else{
          return group;
        }
    }

    //endregion
}
