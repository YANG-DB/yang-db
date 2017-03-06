package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by moti on 3/2/2017.
 */
public class BuilderTestUtil {
    public static Pair<AsgQuery, AsgEBase> CreateSingleEntityQuery(){
        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("Person");
        AsgEBase<EConcrete> ebaseAsgEBase = AsgEBase.EBaseAsgBuilder.<EConcrete>anEBaseAsg().withEBase(concrete).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.EBaseAsgBuilder.<Start>anEBaseAsg().withEBase(start).withNext(ebaseAsgEBase).build();

        AsgQuery query = AsgQuery.AsgQueryBuilder.anAsgQuery().withStart(startAsg).build();

        return new ImmutablePair<>(query, ebaseAsgEBase);
    }

    public static Pair<AsgQuery, AsgEBase<? extends EBase>> createTwoEntitiesPathQuery(){
        EUntyped untyped = new EUntyped();
        untyped.seteNum(3);
        AsgEBase<EUntyped> unTypedAsg3 = AsgEBase.EBaseAsgBuilder.<EUntyped>anEBaseAsg().withEBase(untyped).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        AsgEBase<Rel> relAsg2 = AsgEBase.EBaseAsgBuilder.<Rel>anEBaseAsg().withEBase(rel).withNext(unTypedAsg3).build();

        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("Person");
        AsgEBase<EConcrete> concreteAsg1 = AsgEBase.EBaseAsgBuilder.<EConcrete>anEBaseAsg().withEBase(concrete).withNext(relAsg2).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.EBaseAsgBuilder.<Start>anEBaseAsg().withEBase(start).withNext(concreteAsg1).build();

        AsgQuery query = AsgQuery.AsgQueryBuilder.anAsgQuery().withStart(startAsg).build();

        return  new ImmutablePair<>(query, concreteAsg1);
    }

    public static Plan createPlanForTwoEntitiesPathQuery(AsgQuery asgQuery){
        List<PlanOpBase> ops = new LinkedList<>();

        AsgEBase<Start> startAsg = asgQuery.getStart();
        AsgEBase<? extends EBase> entityAsg = startAsg.getNext().get(0);

        EntityOp concOp = new EntityOp((EEntityBase) entityAsg.geteBase());
        concOp.seteNum(entityAsg.geteNum());
        ops.add(concOp);

        AsgEBase<Rel> relBaseAsg = (AsgEBase<Rel>)entityAsg.getNext().get(0);
        Rel rel = relBaseAsg.geteBase();
        RelationOp relOp = new RelationOp(rel);
        relOp.seteNum(relBaseAsg.geteNum());
        ops.add(relOp);

        AsgEBase<? extends EBase> unBaseAsg = relBaseAsg.getNext().get(0);
        EntityOp unOp = new EntityOp((EEntityBase) unBaseAsg.geteBase());
        ops.add(unOp);

        return new Plan(ops);
    }
}
