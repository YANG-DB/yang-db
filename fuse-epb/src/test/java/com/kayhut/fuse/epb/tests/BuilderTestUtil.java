package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.model.query.EConcrete;
import com.kayhut.fuse.model.query.EUntyped;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.queryAsg.AsgQuery;
import com.kayhut.fuse.model.queryAsg.EBaseAsg;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;

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


        EUntyped untyped = new EUntyped();
        untyped.seteNum(3);

        EBaseAsg untypedBaseAsg = new EBaseAsg();
        untypedBaseAsg.seteBase(untyped);
        relBaseAsg.setNext(new LinkedList<>());
        relBaseAsg.getNext().add(untypedBaseAsg);
        return  new ImmutablePair<>(query, concreteBaseAsg);
    }
}
