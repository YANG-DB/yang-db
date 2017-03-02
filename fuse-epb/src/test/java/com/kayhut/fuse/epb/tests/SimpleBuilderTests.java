package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.epb.plan.PlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.AllDirectionsPlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.CompositePlanExtensionStrategy;
import com.kayhut.fuse.epb.plan.extenders.InitialPlanGeneratorExtensionStrategy;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.query.EConcrete;
import com.kayhut.fuse.model.query.EUntyped;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.queryAsg.AsgQuery;
import com.kayhut.fuse.model.queryAsg.EBaseAsg;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by moti on 2/28/2017.
 */
public class SimpleBuilderTests {

    @Test
    public void TestInitialCreationSingleEntity(){
        AsgQuery query = new AsgQuery();
        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("Person");
        EBaseAsg eBaseAsg = new EBaseAsg();
        eBaseAsg.seteBase(concrete);
        eBaseAsg.seteNum(concrete.geteNum());
        query.setStart(eBaseAsg);

        InitialPlanGeneratorExtensionStrategy strategy = new InitialPlanGeneratorExtensionStrategy();
        Iterable<Plan> plans = strategy.extendPlan(null, query);
        List<Plan> plansList = new LinkedList<>();
        plans.forEach(plansList::add);

        Assert.assertEquals(1,plansList.size());
        Plan plan = plansList.get(0);
        Assert.assertEquals(1,plan.getOps().size());
        PlanOpBase op = plan.getOps().get(0);
        Assert.assertTrue(op instanceof EntityOp);
        Assert.assertEquals(op.geteNum(), eBaseAsg.geteNum());
        Assert.assertEquals(concrete, ((EntityOp)op).getEntity());
    }

    @Test
    public void TestInitialCreationMultipleEntities(){
        AsgQuery query = new AsgQuery();
        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("Person");
        EBaseAsg concreteBaseAsg = new EBaseAsg();
        concreteBaseAsg.seteBase(concrete);
        concreteBaseAsg.seteNum(concrete.geteNum());
        query.setStart(concreteBaseAsg);

        Rel rel = new Rel();
        rel.seteNum(2);
        EBaseAsg relBaseAsg = new EBaseAsg();
        relBaseAsg.seteNum(rel.geteNum());
        relBaseAsg.seteBase(rel);
        concreteBaseAsg.setNext(new LinkedList<>());
        concreteBaseAsg.getNext().add(relBaseAsg);


        EUntyped untyped = new EUntyped();
        untyped.seteNum(3);

        EBaseAsg untypedBaseAsg = new EBaseAsg();
        untypedBaseAsg.seteBase(untyped);
        relBaseAsg.setNext(new LinkedList<>());
        relBaseAsg.getNext().add(untypedBaseAsg);

        InitialPlanGeneratorExtensionStrategy strategy = new InitialPlanGeneratorExtensionStrategy();
        Iterable<Plan> plans = strategy.extendPlan(null, query);
        List<Plan> plansList = new LinkedList<>();
        plans.forEach(plansList::add);

        Assert.assertEquals(2,plansList.size());

        boolean foundFirst = false, foundSecond = false;
        for(Plan plan : plans){
            Assert.assertEquals(1,plan.getOps().size());
            PlanOpBase op = plan.getOps().get(0);
            Assert.assertTrue(op instanceof EntityOp);
            if(((EntityOp)op).getEntity() == concrete){
                foundFirst = true;
                Assert.assertEquals(concreteBaseAsg.geteNum(), op.geteNum());
            }

            if(((EntityOp)op).getEntity() == untyped) {
                foundSecond = true;
                Assert.assertEquals(untypedBaseAsg.geteNum(), op.geteNum());
            }
        }
        Assert.assertTrue(foundFirst && foundSecond);
    }

    @Test
    public void TestCompositePlanStrategyInit(){
        List<PlanExtensionStrategy<Plan, AsgQuery>> extenders = new LinkedList<>();
        extenders.add(new InitialPlanGeneratorExtensionStrategy());
        extenders.add(new AllDirectionsPlanExtensionStrategy());
        CompositePlanExtensionStrategy<Plan, AsgQuery> compositePlanExtensionStrategy = new CompositePlanExtensionStrategy<>(extenders);

        AsgQuery query = new AsgQuery();
        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("Person");
        EBaseAsg eBaseAsg = new EBaseAsg();
        eBaseAsg.seteBase(concrete);
        eBaseAsg.seteNum(concrete.geteNum());
        query.setStart(eBaseAsg);

        Iterable<Plan> plans = compositePlanExtensionStrategy.extendPlan(null, query);
        List<Plan> planList = new LinkedList<>();
        plans.forEach(p -> planList.add(p));

        Assert.assertEquals(1, planList.size());
        Plan plan = planList.get(0);
        Assert.assertEquals(1,plan.getOps().size());
        PlanOpBase op = plan.getOps().get(0);
        Assert.assertTrue(op instanceof EntityOp);
        Assert.assertEquals(op.geteNum(), eBaseAsg.geteNum());
        Assert.assertEquals(concrete, ((EntityOp)op).getEntity());
    }


}
