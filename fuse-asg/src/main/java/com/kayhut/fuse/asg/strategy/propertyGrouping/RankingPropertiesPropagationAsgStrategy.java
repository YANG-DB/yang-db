package com.kayhut.fuse.asg.strategy.propertyGrouping;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RankingProp;
import com.kayhut.fuse.model.query.properties.ScoreEPropGroup;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.ArrayList;
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
