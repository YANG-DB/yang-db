package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.epb.plan.extenders.M1.M1NonRedundantPlanExtensionStrategy;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.entity.GoToEntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.Optional;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.eProp;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.typed;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

/**
 * Created by moti on 04/07/2017.
 */
public class JoinGotoSeedStrategyTest {
    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("1");

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(R);
        rel.setrType("1");

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(3);
        eTyped2.seteTag("B");
        eTyped2.seteType("2");

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(rel)
                                        .withNext(AsgEBase.Builder.get().withEBase(eTyped2)
                                                .build())
                                        .build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    public static AsgQuery simpleQuery2(){
        return AsgQuery.Builder.start("Q1", "Dragons").
                next(typed(1, OntologyTestUtils.PERSON.type)).
                next(eProp(2, EProp.of(FIRST_NAME.type, 2, Constraint.of(ConstraintOp.ge, "g")))).
                next(rel(3, OWN.getrType(), Rel.Direction.R).below(relProp(4, RelProp.of(START_DATE.type, 4, Constraint.of(ConstraintOp.ge, 123))))).
                next(typed(5, OntologyTestUtils.DRAGON.type)).
                next(eProp(6, EProp.of(NAME.type,6, Constraint.of(ConstraintOp.ge,"g")))).
                next(rel(7, OWN.getrType(), Rel.Direction.R).below(relProp(8, RelProp.of(START_DATE.type, 8, Constraint.of(ConstraintOp.ge, 123))))).
                next(typed(9, OntologyTestUtils.DRAGON.type)).
                next(eProp(10, EProp.of(NAME.type,10, Constraint.of(ConstraintOp.ge,"g")))).
                build();
    }

    @Test
    public void emptyPlanTest(){
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        M1NonRedundantPlanExtensionStrategy m1ExtensionStrategy = new M1NonRedundantPlanExtensionStrategy();
        GotoExtensionStrategy gotoExtensionStrategy = new GotoExtensionStrategy();
        JoinGotoSeedExtensionStrategy join = new JoinGotoSeedExtensionStrategy(m1ExtensionStrategy, gotoExtensionStrategy);
        Iterable<Plan> plans = join.extendPlan(Optional.empty(), asgQuery);
        Assert.assertEquals(0, Stream.ofAll(plans).length());
    }

    @Test
    public void singleEntityPlanTest(){
        AsgQuery asgQuery = simpleQuery2();
        Plan plan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 4)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 6))
                );
        M1NonRedundantPlanExtensionStrategy m1ExtensionStrategy = new M1NonRedundantPlanExtensionStrategy();
        GotoExtensionStrategy gotoExtensionStrategy = new GotoExtensionStrategy();
        JoinGotoSeedExtensionStrategy join = new JoinGotoSeedExtensionStrategy(m1ExtensionStrategy, gotoExtensionStrategy);
        Iterable<Plan> plans = join.extendPlan(Optional.of(plan), asgQuery);
        Assert.assertEquals(3, Stream.ofAll(plans).length());

        Plan leftBranch = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 2)),
                new RelationOp(AsgQueryUtil.element$(asgQuery, 3)),
                new RelationFilterOp(AsgQueryUtil.element$(asgQuery, 4)),
                new EntityOp(AsgQueryUtil.element$(asgQuery, 5)),
                new EntityFilterOp(AsgQueryUtil.element$(asgQuery, 6)),
                new GoToEntityOp(AsgQueryUtil.element$(asgQuery, 1))
        );

        plans.forEach(p -> Assert.assertEquals(leftBranch, ((EntityJoinOp)p.getOps().get(0)).getLeftBranch()));

    }


}
