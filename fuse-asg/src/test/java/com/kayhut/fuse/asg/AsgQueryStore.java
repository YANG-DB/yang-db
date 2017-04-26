package com.kayhut.fuse.asg;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;

/**
 * Created by Roman on 25/04/2017.
 */
public class AsgQueryStore {
    public static AsgQuery startXeTypedXrelXeTypedXXX(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType(1);

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(Rel.Direction.R);
        rel.setrType(1);

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(3);
        eTyped2.seteTag("B");
        eTyped2.seteType(2);

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

    public static AsgQuery startXeTypedXrelXeTypedXQuant1XrelXunTypedX_relXconcreteXXXXXX(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType(1);

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(Rel.Direction.R);
        rel.setrType(1);

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(3);
        eTyped2.seteTag("B");
        eTyped2.seteType(2);

        Quant1 quant1 = new Quant1();
        quant1.seteNum(4);
        quant1.setqType(QuantType.all);

        Rel rel2 = new Rel();
        rel2.seteNum(5);
        rel2.setDir(Rel.Direction.R);
        rel2.setrType(4);

        EUntyped untyped = new EUntyped();
        untyped.seteNum(6);
        untyped.seteTag("C");

        Rel rel3 = new Rel();
        rel3.seteNum(7);
        rel3.setDir(Rel.Direction.R);
        rel3.setrType(5);

        EConcrete concrete = new EConcrete();
        concrete.seteID("concrete1");
        concrete.seteName("Concrete1");
        concrete.seteType(3);
        concrete.seteNum(8);
        concrete.seteTag("D");

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(rel)
                                        .withNext(AsgEBase.Builder.get().withEBase(eTyped2)
                                                .withNext(AsgEBase.Builder.get().withEBase(quant1)
                                                        .withNext(AsgEBase.Builder.get().withEBase(rel2)
                                                                .withNext(AsgEBase.Builder.get().withEBase(untyped)
                                                                        .build())
                                                                .build())
                                                        .withNext(AsgEBase.Builder.get().withEBase(rel3)
                                                                .withNext(AsgEBase.Builder.get().withEBase(concrete)
                                                                        .build())
                                                                .build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }
}
