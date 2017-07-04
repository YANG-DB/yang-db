package com.kayhut.fuse.epb.plan.extenders;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.epb.plan.seeders.M1PlanSeedStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.JoinOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * Created by moti on 04/07/2017.
 */
public class JoinSeedStrategyTest {
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

    @Test
    public void emptyPlanTest(){
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        M1PlanSeedStrategy m1Strategy = new M1PlanSeedStrategy();
        JoinSeedExtensionStrategy join = new JoinSeedExtensionStrategy(m1Strategy);
        Iterable<Plan> plans = join.extendPlan(new Plan(), asgQuery);
        Assert.assertEquals(0, Stream.ofAll(plans).length());
    }

    @Test
    public void singleEntityPlanTest(){
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan plan = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));
        M1PlanSeedStrategy m1Strategy = new M1PlanSeedStrategy();
        JoinSeedExtensionStrategy join = new JoinSeedExtensionStrategy(m1Strategy);
        Iterable<Plan> plans = join.extendPlan(plan, asgQuery);
        Assert.assertEquals(2, Stream.ofAll(plans).length());
        plans.forEach(p -> Assert.assertEquals(plan,((JoinOp)p.getOps().get(0)).getLeftBranch()));
    }


}
