package com.kayhut.fuse.asg.strategy.propertyGrouping;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.ScoreEProp;
import com.kayhut.fuse.model.query.properties.ScoreEPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.QuantType;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AsgRankingPropagationGroupingStrategyTest {

    //This Eprop is not under an AND quantifier and should be replaced by the EPropGroup Element -  e.g. Q3 on V1
    public static AsgQuery simpleBoostingQuery(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("1");

        EPropGroup group = new EPropGroup(2);
        group.getProps().add(new ScoreEProp(2, "stringValue", Constraint.of(ConstraintOp.eq, "abc"), 100));

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(group).build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    public static AsgQuery oneHierarchyBoostingQuery(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("1");

        EPropGroup group = new EPropGroup(2);
        group.setQuantType(QuantType.all);
        group.getProps().add(new EProp(2, "fieldId", Constraint.of(ConstraintOp.eq, "fieldName")));
        group.getProps().add(new EProp(2, "stringValue", Constraint.of(ConstraintOp.eq, "abc")));

        EPropGroup innerGroup = new EPropGroup(3);
        innerGroup.setQuantType(QuantType.some);
        innerGroup.getProps().add(new ScoreEProp(3, "stringValue", Constraint.of(ConstraintOp.likeAny, "*abc"),100));
        group.getGroups().add(innerGroup );

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(group).build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    public static AsgQuery twoHierarchyBoostingQuery(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("1");

        EPropGroup group = new EPropGroup(2);
        group.setQuantType(QuantType.all);
        group.getProps().add(new EProp(2, "fieldId", Constraint.of(ConstraintOp.eq, "fieldName")));
        group.getProps().add(new EProp(2, "stringValue", Constraint.of(ConstraintOp.eq, "abc")));

        EPropGroup innerGroup = new EPropGroup(3);
        innerGroup.setQuantType(QuantType.some);
        innerGroup.getProps().add(new EProp(3, "stringValue", Constraint.of(ConstraintOp.likeAny, "*abc")));
        group.getGroups().add(innerGroup );

        innerGroup = new EPropGroup(4);
        innerGroup.setQuantType(QuantType.some);
        innerGroup.getProps().add(new ScoreEProp(4, "stringValue", Constraint.of(ConstraintOp.likeAny, "*abc"),100));
        group.getGroups().get(0).getGroups().add(innerGroup );

        innerGroup = new EPropGroup(5);
        innerGroup.setQuantType(QuantType.some);
        innerGroup.getProps().add(new EProp(4, "stringValue", Constraint.of(ConstraintOp.likeAny, "*abc")));
        group.getGroups().get(0).getGroups().add(innerGroup );

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(group).build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    public static AsgQuery threeHierarchyBoostingQuery(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("1");

        EPropGroup group = new EPropGroup(2);//change
        group.setQuantType(QuantType.all);
        group.getProps().add(new EProp(2, "fieldId", Constraint.of(ConstraintOp.eq, "fieldName")));
        group.getProps().add(new EProp(2, "stringValue", Constraint.of(ConstraintOp.eq, "abc")));

        EPropGroup innerGroup = new EPropGroup(3);
        innerGroup.setQuantType(QuantType.some);
        innerGroup.getProps().add(new EProp(3, "stringValue", Constraint.of(ConstraintOp.likeAny, "*abc")));
        group.getGroups().add(innerGroup );

        innerGroup = new EPropGroup(4);
        innerGroup.setQuantType(QuantType.some);
        innerGroup.getProps().add(new ScoreEProp(4, "stringValue", Constraint.of(ConstraintOp.likeAny, "*abc"),100));
        group.getGroups().add(innerGroup );

        innerGroup = new EPropGroup(5);
        innerGroup.setQuantType(QuantType.some);
        innerGroup.getProps().add(new EProp(4, "stringValue", Constraint.of(ConstraintOp.likeAny, "*abc")));
        group.getGroups().get(0).getGroups().add(innerGroup );

        innerGroup = new EPropGroup(6);
        innerGroup.setQuantType(QuantType.all);
        innerGroup.getProps().add(new EProp(6, "stringValue", Constraint.of(ConstraintOp.likeAny, "*abc")));
        innerGroup.getProps().add(new EProp(6, "stringValue", Constraint.of(ConstraintOp.likeAny, "*abc")));
        group.getGroups().get(1).getGroups().add(innerGroup );

        innerGroup = new EPropGroup(7);//change
        innerGroup.setQuantType(QuantType.all);
        innerGroup.getProps().add(new EProp(7, "stringValue", Constraint.of(ConstraintOp.likeAny, "*abc")));
        innerGroup.getProps().add(new ScoreEProp(7, "stringValue", Constraint.of(ConstraintOp.likeAny, "*abc"),10));
        group.getGroups().get(1).getGroups().add(innerGroup );

        innerGroup = new EPropGroup(8);//change
        innerGroup.setQuantType(QuantType.all);
        innerGroup.getProps().add(new EProp(8, "stringValue", Constraint.of(ConstraintOp.likeAny, "*abc")));
        innerGroup.getProps().add(new ScoreEProp(8, "stringValue", Constraint.of(ConstraintOp.likeAny, "*abc"),10));
        group.getGroups().get(0).getGroups().get(0).getGroups().add(innerGroup);

        innerGroup = new EPropGroup(9);//change
        innerGroup.setQuantType(QuantType.all);
        innerGroup.getProps().add(new EProp(9, "stringValue", Constraint.of(ConstraintOp.likeAny, "*abc")));
        innerGroup.getProps().add(new EProp(9, "stringValue", Constraint.of(ConstraintOp.likeAny, "*abc")));
        group.getGroups().get(0).getGroups().get(0).getGroups().add(innerGroup);

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(group).build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    @Test
    public void noHierarchyRankingGroup()  {
        AsgQuery query = simpleBoostingQuery("test", "knowledge");
        RankingPropertiesPropagationAsgStrategy strategy = new RankingPropertiesPropagationAsgStrategy();
        strategy.apply(query,new AsgStrategyContext(null));

        Optional<AsgEBase<EBase>> group = AsgQueryUtil.get(query.getStart(), 2);
        assertTrue(group.get().geteBase() instanceof ScoreEPropGroup);
        assertEquals(((ScoreEPropGroup) group.get().geteBase()).getBoost(),1 );
    }

    @Test
    public void oneHierarchyRankingGroup() {
        AsgQuery query = oneHierarchyBoostingQuery("test", "knowledge");
        RankingPropertiesPropagationAsgStrategy strategy = new RankingPropertiesPropagationAsgStrategy();
        strategy.apply(query,new AsgStrategyContext(null));

        Optional<AsgEBase<EBase>> groupElement = AsgQueryUtil.get(query.getStart(), 2);
        assertTrue(groupElement.get().geteBase() instanceof ScoreEPropGroup);
        ScoreEPropGroup group = (ScoreEPropGroup) groupElement.get().geteBase();
        assertEquals(group.getBoost(),1 );
        EPropGroup innerGroup = group.getGroups().get(0);
        assertTrue(innerGroup instanceof ScoreEPropGroup);
        assertEquals(((ScoreEPropGroup) innerGroup).getBoost(),1 );
    }

    @Test
    public void twoHierarchyRankingGroup() {
        AsgQuery query = twoHierarchyBoostingQuery("test", "knowledge");
        RankingPropertiesPropagationAsgStrategy strategy = new RankingPropertiesPropagationAsgStrategy();
        strategy.apply(query,new AsgStrategyContext(null));

        Optional<AsgEBase<EBase>> groupElement = AsgQueryUtil.get(query.getStart(), 2);
        assertTrue(groupElement.get().geteBase() instanceof ScoreEPropGroup);
        ScoreEPropGroup group = (ScoreEPropGroup) groupElement.get().geteBase();
        assertEquals(group.getBoost(),1 );
        EPropGroup innerGroup = group.getGroups().get(0);
        assertTrue(innerGroup instanceof ScoreEPropGroup);
        assertEquals(((ScoreEPropGroup) innerGroup).getBoost(),1 );
    }

    @Test
    public void threeHierarchyRankingGroup() {
        AsgQuery query = threeHierarchyBoostingQuery("test", "knowledge");
        RankingPropertiesPropagationAsgStrategy strategy = new RankingPropertiesPropagationAsgStrategy();
        strategy.apply(query,new AsgStrategyContext(null));

        Optional<AsgEBase<EBase>> groupElement = AsgQueryUtil.get(query.getStart(), 2);
        assertTrue(groupElement.get().geteBase() instanceof ScoreEPropGroup);
        ScoreEPropGroup group = (ScoreEPropGroup) groupElement.get().geteBase();
        assertEquals(group.getBoost(),1 );

        //group 3
        EPropGroup innerGroup = group.getGroups().get(0);
        assertTrue(innerGroup instanceof ScoreEPropGroup);
        assertEquals(((ScoreEPropGroup) innerGroup).getBoost(),1 );

        //group 5
        innerGroup = innerGroup.getGroups().get(0);
        assertTrue(innerGroup instanceof ScoreEPropGroup);

        //group 8
        assertEquals(8, innerGroup.getGroups().get(0).geteNum());
        assertTrue(innerGroup.getGroups().get(0) instanceof ScoreEPropGroup);

        //group 9
        assertEquals(9, innerGroup.getGroups().get(1).geteNum());
        assertFalse(innerGroup.getGroups().get(1) instanceof ScoreEPropGroup);

        //group 2
        innerGroup = group.getGroups().get(1);
        assertTrue(innerGroup instanceof ScoreEPropGroup);
        assertEquals(((ScoreEPropGroup) innerGroup).getBoost(),1 );

        //group 7
        innerGroup = innerGroup.getGroups().get(1);
        assertTrue(innerGroup instanceof ScoreEPropGroup);
        assertEquals(((ScoreEPropGroup) innerGroup).getBoost(),1 );
    }
}