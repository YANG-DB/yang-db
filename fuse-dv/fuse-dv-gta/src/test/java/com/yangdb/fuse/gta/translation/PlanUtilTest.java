package com.yangdb.fuse.gta.translation;

import com.yangdb.fuse.dispatcher.utils.PlanUtil;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.*;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.EUntyped;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by benishue on 13-Mar-17.
 */
public class PlanUtilTest {

    private static Plan planOf2;

    @Test
    public void isFirst() throws Exception {
        PlanOp planOpFirst = planOf2.getOps().get(0);
        PlanOp planOpSecond = planOf2.getOps().get(1);

        assertTrue(PlanUtil.isFirst(planOf2, planOpFirst));
        assertFalse(PlanUtil.isFirst(planOf2, planOpSecond));
    }
    @Test
    public void isLast() throws Exception {
        PlanOp planOpFirst = planOf2.getOps().get(0);
        PlanOp planOpLast= planOf2.getOps().get(planOf2.getOps().size()-1);

        assertTrue(PlanUtil.isLast(planOf2, planOpLast));
        assertFalse(PlanUtil.isLast(planOf2, planOpFirst));
    }

    @Test
    public void getNext() throws Exception {
        PlanOp planOpFirst = planOf2.getOps().get(0);
        PlanOp planOpSecond = planOf2.getOps().get(1);

        assertEquals(planOpSecond, PlanUtil.adjacentNext(planOf2, planOpFirst).get());
        assertNotEquals(planOpFirst, PlanUtil.adjacentNext(planOf2, planOpSecond).get());
    }

    @Test
    public void getPrev() throws Exception {
        PlanOp planOpFirst = planOf2.getOps().get(0);
        PlanOp planOpSecond = planOf2.getOps().get(1);

        assertTrue(!PlanUtil.adjacentPrev(planOf2, planOpFirst).isPresent());
        assertEquals(planOpFirst, PlanUtil.adjacentPrev(planOf2, planOpSecond).get());
    }


    @BeforeClass
    public static void setUpOnce() {
        createPlanOf2();
    }

    private static void createPlanOf2() {
        AsgQuery twoEntitiesPathQuery = createTwoEntitiesPathQuery();
        planOf2 = createPlanForTwoEntitiesPathQuery(twoEntitiesPathQuery);
    }

    public static AsgQuery createTwoEntitiesPathQuery() {
        EUntyped untyped = new EUntyped();
        untyped.seteNum(3);
        AsgEBase<EUntyped> unTypedAsg3 = AsgEBase.Builder.<EUntyped>get().withEBase(untyped).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        AsgEBase<Rel> relAsg2 = AsgEBase.Builder.<Rel>get().withEBase(rel).withNext(unTypedAsg3).build();

        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("A");
        AsgEBase<EConcrete> concreteAsg1 = AsgEBase.Builder.<EConcrete>get().withEBase(concrete).withNext(relAsg2).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.Builder.<Start>get().withEBase(start).withNext(concreteAsg1).build();

        AsgQuery query = AsgQuery.AsgQueryBuilder.anAsgQuery().withStart(startAsg).build();

        return query;
    }

    public static Plan createPlanForTwoEntitiesPathQuery(AsgQuery asgQuery) {
        List<PlanOp> ops = new LinkedList<>();

        AsgEBase<Start> startAsg = asgQuery.getStart();
        AsgEBase<EEntityBase> entityAsg = (AsgEBase<EEntityBase>) startAsg.getNext().get(0);

        EntityOp concOp = new EntityOp(entityAsg);
        ops.add(concOp);

        AsgEBase<Rel> relBaseAsg = (AsgEBase<Rel>) entityAsg.getNext().get(0);
        RelationOp relOp = new RelationOp(relBaseAsg);
        ops.add(relOp);

        AsgEBase<EEntityBase> unBaseAsg = (AsgEBase<EEntityBase>) relBaseAsg.getNext().get(0);
        EntityOp unOp = new EntityOp(unBaseAsg);
        ops.add(unOp);

        return new Plan(ops);
    }

}