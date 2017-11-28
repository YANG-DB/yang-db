package com.kayhut.fuse.epb.utils;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
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
        AsgEBase<EConcrete> ebaseAsgEBase = AsgEBase.Builder.<EConcrete>get().withEBase(concrete).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.Builder.<Start>get().withEBase(start).withNext(ebaseAsgEBase).build();

        AsgQuery query = AsgQuery.AsgQueryBuilder.anAsgQuery().withStart(startAsg).build();

        return new ImmutablePair<>(query, ebaseAsgEBase);
    }

    public static Pair<AsgQuery, AsgEBase<? extends EBase>> createTwoEntitiesPathQuery(){
        EUntyped untyped = new EUntyped();
        untyped.seteNum(3);
        AsgEBase<EUntyped> unTypedAsg3 = AsgEBase.Builder.<EUntyped>get().withEBase(untyped).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(Rel.Direction.R);
        AsgEBase<Rel> relAsg2 = AsgEBase.Builder.<Rel>get().withEBase(rel).withNext(unTypedAsg3).build();

        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("Person");
        AsgEBase<EConcrete> concreteAsg1 = AsgEBase.Builder.<EConcrete>get().withEBase(concrete).withNext(relAsg2).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.Builder.<Start>get().withEBase(start).withNext(concreteAsg1).build();

        AsgQuery query = AsgQuery.AsgQueryBuilder.anAsgQuery().withStart(startAsg).build();

        return  new ImmutablePair<>(query, concreteAsg1);
    }

    public static Plan createPlanForTwoEntitiesPathQuery(AsgQuery asgQuery){
        List<PlanOp> ops = new LinkedList<>();

        AsgEBase<Start> startAsg = asgQuery.getStart();
        AsgEBase<EEntityBase> entityAsg = (AsgEBase<EEntityBase>) startAsg.getNext().get(0);

        EntityOp concOp = new EntityOp(entityAsg);
        ops.add(concOp);

        AsgEBase<Rel> relBaseAsg = (AsgEBase<Rel>)entityAsg.getNext().get(0);
        RelationOp relOp = new RelationOp(relBaseAsg);
        ops.add(relOp);

        AsgEBase<EEntityBase> unBaseAsg = (AsgEBase<EEntityBase>)relBaseAsg.getNext().get(0);
        EntityOp unOp = new EntityOp(unBaseAsg);
        ops.add(unOp);

        return new Plan(ops);
    }
}
