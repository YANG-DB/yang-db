package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.query.EConcrete;
import com.kayhut.fuse.model.query.EEntityBase;
import com.kayhut.fuse.model.query.EUntyped;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.queryAsg.AsgQuery;
import com.kayhut.fuse.model.queryAsg.EBaseAsg;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.management.relation.Relation;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by moti on 3/2/2017.
 */
public class BuilderTestUtil {
    public static Pair<AsgQuery, EBaseAsg> CreateSingleEntityQuery(){
        AsgQuery query = new AsgQuery();
        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("Person");
        EBaseAsg eBaseAsg = new EBaseAsg();
        eBaseAsg.seteBase(concrete);
        eBaseAsg.seteNum(concrete.geteNum());
        query.setStart(eBaseAsg);
        return new ImmutablePair<>(query, eBaseAsg);
    }

    public static Pair<AsgQuery, EBaseAsg> createTwoEntitiesPathQuery(){
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
        concreteBaseAsg.setB(new LinkedList<>());

        EUntyped untyped = new EUntyped();
        untyped.seteNum(3);

        EBaseAsg untypedBaseAsg = new EBaseAsg();
        untypedBaseAsg.seteBase(untyped);
        relBaseAsg.setNext(new LinkedList<>());
        relBaseAsg.getNext().add(untypedBaseAsg);
        relBaseAsg.setB(new LinkedList<>());
        return  new ImmutablePair<>(query, concreteBaseAsg);
    }

    public static Plan createPlanForTwoEntitiesPathQuery(AsgQuery asgQuery){
        List<PlanOpBase> ops = new LinkedList<>();
        EBaseAsg start = asgQuery.getStart();
        EntityOp concOp = new EntityOp((EEntityBase) start.geteBase());
        concOp.seteNum(asgQuery.getStart().geteNum());
        ops.add(concOp);
        EBaseAsg relBaseAsg = start.getNext().get(0);
        Rel rel = (Rel)relBaseAsg.geteBase();
        RelationOp relOp = new RelationOp();
        relOp.setRelation(rel);
        relOp.seteNum(relBaseAsg.geteNum());
        ops.add(relOp);
        EBaseAsg unBaseAsg = relBaseAsg.getNext().get(0);
        EntityOp unOp = new EntityOp((EEntityBase) unBaseAsg.geteBase());
        unBaseAsg.seteNum(unBaseAsg.geteNum());
        ops.add(unOp);
        return new Plan(ops);
    }
}
