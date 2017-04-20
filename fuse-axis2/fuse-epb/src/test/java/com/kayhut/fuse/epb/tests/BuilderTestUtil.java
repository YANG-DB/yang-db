package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpWithCost;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.EUntyped;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by moti on 3/2/2017.
 */
public class BuilderTestUtil {
    public static Pair<AsgQuery, AsgEBase> createSingleEntityQuery(){
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
        rel.setDir("R");
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

    public static Plan<Cost> createPlanForTwoEntitiesPathQuery(AsgQuery asgQuery){
        List<PlanOpWithCost<Cost>> ops = new LinkedList<>();

        AsgEBase<Start> startAsg = asgQuery.getStart();
        AsgEBase<EEntityBase> entityAsg = (AsgEBase<EEntityBase>) startAsg.getNext().get(0);

        EntityOp concOp = new EntityOp(entityAsg);
        ops.add(new PlanOpWithCost<>(concOp, new Cost(0,0,0)));

        AsgEBase<Rel> relBaseAsg = (AsgEBase<Rel>)entityAsg.getNext().get(0);
        RelationOp relOp = new RelationOp(relBaseAsg);
        ops.add(new PlanOpWithCost<>(relOp, new Cost(0,0,0)));

        AsgEBase<EEntityBase> unBaseAsg = (AsgEBase<EEntityBase>)relBaseAsg.getNext().get(0);
        EntityOp unOp = new EntityOp(unBaseAsg);
        ops.add(new PlanOpWithCost<>(unOp, new Cost(0,0,0)));

        return Plan.PlanBuilder.build(ops).compose();
    }
}
