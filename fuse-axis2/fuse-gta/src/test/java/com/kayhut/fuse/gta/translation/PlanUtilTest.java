package com.kayhut.fuse.gta.translation;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.EUntyped;
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
        List<PlanOpBase> ops = planOf2.getOps();
        PlanOpBase planOpBaseFirst = planOf2.getOps().get(0);
        PlanOpBase planOpBaseSecond = planOf2.getOps().get(1);

        PlanUtil planUtil = new PlanUtil();
        assertTrue(planUtil.isFirst(ops,planOpBaseFirst));
        assertFalse(planUtil.isFirst(ops,planOpBaseSecond));
    }

    @Test
    public void getNext() throws Exception {

        List<PlanOpBase> ops = planOf2.getOps();
        PlanOpBase planOpBaseFirst = planOf2.getOps().get(0);
        PlanOpBase planOpBaseSecond = planOf2.getOps().get(1);

        PlanUtil planUtil = new PlanUtil();
        assertEquals(planOpBaseSecond,planUtil.getNext(ops,planOpBaseFirst).get());
        assertNotEquals(planOpBaseFirst,planUtil.getNext(ops,planOpBaseSecond).get());
    }

    @Test
    public void getPrev() throws Exception {
        List<PlanOpBase> ops = planOf2.getOps();
        PlanOpBase planOpBaseFirst = planOf2.getOps().get(0);
        PlanOpBase planOpBaseSecond = planOf2.getOps().get(1);

        PlanUtil planUtil = new PlanUtil();
        assertTrue(!planUtil.getPrev(ops,planOpBaseFirst).isPresent());
        assertEquals(planOpBaseFirst,planUtil.getPrev(ops,planOpBaseSecond).get());
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
        AsgEBase<EUntyped> unTypedAsg3 = AsgEBase.EBaseAsgBuilder.<EUntyped>anEBaseAsg().withEBase(untyped).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        AsgEBase<Rel> relAsg2 = AsgEBase.EBaseAsgBuilder.<Rel>anEBaseAsg().withEBase(rel).withNext(unTypedAsg3).build();

        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("A");
        AsgEBase<EConcrete> concreteAsg1 = AsgEBase.EBaseAsgBuilder.<EConcrete>anEBaseAsg().withEBase(concrete).withNext(relAsg2).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.EBaseAsgBuilder.<Start>anEBaseAsg().withEBase(start).withNext(concreteAsg1).build();

        AsgQuery query = AsgQuery.AsgQueryBuilder.anAsgQuery().withStart(startAsg).build();

        return query;
    }

    public static Plan createPlanForTwoEntitiesPathQuery(AsgQuery asgQuery) {
        List<PlanOpBase> ops = new LinkedList<>();

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