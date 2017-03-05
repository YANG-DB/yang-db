package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.queryAsg.AsgQuery;
import com.kayhut.fuse.model.queryAsg.EBaseAsg;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.management.relation.Relation;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by moti on 3/2/2017.
 */
public class BuilderTestUtil {
    public static Pair<AsgQuery, EBaseAsg> CreateSingleEntityQuery(){
        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("Person");
        EBaseAsg<EConcrete> ebaseAsg = EBaseAsg.EBaseAsgBuilder.<EConcrete>anEBaseAsg().withEBase(concrete).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        EBaseAsg<Start> startAsg = EBaseAsg.EBaseAsgBuilder.<Start>anEBaseAsg().withEBase(start).withNext(ebaseAsg).build();

        AsgQuery query = AsgQuery.AsgQueryBuilder.anAsgQuery().withStart(startAsg).build();

        return new ImmutablePair<>(query, ebaseAsg);
    }

    public static Pair<AsgQuery, EBaseAsg<? extends EBase>> createTwoEntitiesPathQuery(){
        EUntyped untyped = new EUntyped();
        untyped.seteNum(3);
        EBaseAsg<EUntyped> unTypedAsg3 = EBaseAsg.EBaseAsgBuilder.<EUntyped>anEBaseAsg().withEBase(untyped).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        EBaseAsg<Rel> relAsg2 = EBaseAsg.EBaseAsgBuilder.<Rel>anEBaseAsg().withEBase(rel).withNext(unTypedAsg3).build();

        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("Person");
        EBaseAsg<EConcrete> concreteAsg1 = EBaseAsg.EBaseAsgBuilder.<EConcrete>anEBaseAsg().withEBase(concrete).withNext(relAsg2).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        EBaseAsg<Start> startAsg = EBaseAsg.EBaseAsgBuilder.<Start>anEBaseAsg().withEBase(start).withNext(concreteAsg1).build();

        AsgQuery query = AsgQuery.AsgQueryBuilder.anAsgQuery().withStart(startAsg).build();

        return  new ImmutablePair<>(query, concreteAsg1);
    }

    public static Plan createPlanForTwoEntitiesPathQuery(AsgQuery asgQuery){
        List<PlanOpBase> ops = new LinkedList<>();

        EBaseAsg<Start> startAsg = asgQuery.getStart();
        EBaseAsg<? extends EBase> entityAsg = startAsg.getNext().get(0);

        EntityOp concOp = new EntityOp((EEntityBase) entityAsg.geteBase());
        concOp.seteNum(entityAsg.geteNum());
        ops.add(concOp);

        EBaseAsg<Rel> relBaseAsg = (EBaseAsg<Rel>)entityAsg.getNext().get(0);
        Rel rel = relBaseAsg.geteBase();
        RelationOp relOp = new RelationOp(rel);
        relOp.seteNum(relBaseAsg.geteNum());
        ops.add(relOp);

        EBaseAsg<? extends EBase> unBaseAsg = relBaseAsg.getNext().get(0);
        EntityOp unOp = new EntityOp((EEntityBase) unBaseAsg.geteBase());
        ops.add(unOp);

        return new Plan(ops);
    }
}
