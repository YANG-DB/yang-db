package com.kayhut.fuse.asg.strategy.propertyGrouping;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RankingProp;
import com.kayhut.fuse.model.query.properties.ScoreEPropGroup;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;

public class RankingPropertiesPropagationAsgStrategy implements AsgStrategy {
    /*
     * propagating ranking properties upwards (wrapped containers with RankingProp Interface)
     */

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        //go over all EPropGroups and ....
        Stream.ofAll(AsgQueryUtil.elements(query, asgEBase -> asgEBase.geteBase() instanceof EPropGroup))
                .forEach(property -> {
                    List<EPropGroup> list = new ArrayList<>();
                    EPropGroup group = (EPropGroup) property.geteBase();
                    //verify if the group itself contains RankingProp properties
                    if (!Stream.ofAll(group.getProps()).find(p -> p instanceof RankingProp).isEmpty()) {
                        list.add(group);
                    }
                    //find if inner groups contain RankingProp property
                    list.addAll(EPropGroup.findInGroupRecursive(group,
                            ePropGroup ->
                                    ePropGroup.getProps().stream().anyMatch(p -> p instanceof RankingProp) ||
                                            ePropGroup.getGroups().stream().anyMatch(p -> p instanceof RankingProp)
                    ));

                    if(!list.isEmpty()) {
                        ScoreEPropGroup eBase = new ScoreEPropGroup(list.get(list.size() - 1), 1);
                        property.seteBase(eBase);
                        //downstream replace all EPropGroups with ScoreEPropGroup when applicable
                        replaceRecursive(eBase, list);
                    }
                });
    }

    private void replaceRecursive(EPropGroup eBase, List<EPropGroup> list) {
        List<EPropGroup> groups = Stream.ofAll(eBase.getGroups())
                .filter(p -> list.contains(p))
                .filter(p -> !(p instanceof ScoreEPropGroup))
                .toJavaList();
        groups.forEach(
                present -> {
                    eBase.getGroups().remove(present);
                    eBase.getGroups().add(new ScoreEPropGroup(present, 1));
                });
        //search recursively
        eBase.getGroups().forEach(grp -> replaceRecursive(grp, list));
    }
    //endregion
}
