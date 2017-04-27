package com.kayhut.fuse.asg;

import com.google.common.base.Supplier;
import com.kayhut.fuse.asg.builder.RecTwoPassAsgQuerySupplier;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.HQuant;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static AsgQuery Q188_V1(){
        //region Query Building
        Query query = new Query(); //Person owns Dragon with EProp - Name: 'dragonA'
        query.setOnt("Dragons");
        query.setName("Q188");
        List<EBase> elements = new ArrayList<EBase>();

        /*
        {
          "eNum": 0,
          "type": "Start",
          "next": 1
        }
         */

        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);

        /*
        {
            "eNum": 1,
            "type": "EConcrete",
            "eTag": "A",
            "eID": "1234",
            "eType": 2,
            "eName": "Balerion",
            "next": 2
        }
        */

        EConcrete eConcrete = new EConcrete();
        eConcrete.seteNum(1);
        eConcrete.seteTag("A");
        eConcrete.seteID("1234");
        eConcrete.seteType(2);
        eConcrete.seteName("Balerion");
        eConcrete.setNext(2);
        elements.add(eConcrete);

        /*
        {
            "eNum": 2,
            "type": "Rel",
            "rType": 3,
            "dir": "R",
            "next": 3,
            "b": 4
        }
        */

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType(3);
        rel.setDir(Rel.Direction.R);
        rel.setNext(3);
        rel.setB(4);
        elements.add(rel);

        /*
        {
            "eNum": 3,
            "type": "ETyped",
            "eTag": "B",
            "eType": 2
        }
        */

        ETyped eTyped = new ETyped();
        eTyped.seteNum(3);
        eTyped.seteTag("B");
        eTyped.seteType(2);
        elements.add(eTyped);

        /*
        {
            "eNum": 4,
            "type": "RelProp",
            "pType": "1",
            "pTag": "1",
            "con": {
                "op": "gt",
               "expr": "1010-01-01T00:00:00.000"
            },
            "b": 5
        }
        */

        Constraint conRelProp1 = new Constraint();
        conRelProp1.setOp(ConstraintOp.gt);
        conRelProp1.setExpr("1010-01-01T00:00:00.000");

        RelProp relProp1 = new RelProp();
        relProp1.seteNum(4);
        relProp1.setpType("1");
        relProp1.setpTag("1");
        relProp1.setCon(conRelProp1);
        relProp1.setB(5);
        elements.add(relProp1);

        /*
        {
            "eNum": 5,
            "type": "RelProp",
            "pType": "2",
            "pTag": "2",
            "con": {
                "op": "lt",
                "expr": "10"
            }
        }
        */

        Constraint conRelProp2 = new Constraint();
        conRelProp2.setOp(ConstraintOp.lt);
        conRelProp2.setExpr("10");
        RelProp relProp2 = new RelProp();
        relProp2.seteNum(5);
        relProp2.setpType("2");
        relProp2.setpTag("2");
        relProp2.setCon(conRelProp2);
        elements.add(relProp2);

        query.setElements(elements);


        Supplier<AsgQuery> asgSupplier = new RecTwoPassAsgQuerySupplier(query);
        AsgQuery asgQuery = asgSupplier.get();
        return  asgQuery;
    }

    public static AsgQuery Q187_V1(){
        //region Query Building
        Query query = new Query(); //Person owns Dragon with EProp - Name: 'dragonA'
        query.setOnt("Dragons");
        query.setName("Q187");
        List<EBase> elements = new ArrayList<EBase>();

        /*
        {
          "eNum": 0,
          "type": "Start",
          "next": 1
        }
         */

        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);

        /*
        {
            "eNum": 1,
            "type": "EConcrete",
            "eTag": "A",
            "eID": "1234",
            "eType": 2,
            "eName": "Balerion",
            "next": 2
        }
        */

        EConcrete eConcrete = new EConcrete();
        eConcrete.seteNum(1);
        eConcrete.seteTag("A");
        eConcrete.seteID("1234");
        eConcrete.seteType(2);
        eConcrete.seteName("Balerion");
        eConcrete.setNext(2);
        elements.add(eConcrete);

        /*
        {
            "eNum": 2,
            "type": "Rel",
            "rType": 3,
            "dir": "R",
            "next": 3,
            "b": 4
        }
        */

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType(3);
        rel.setDir(Rel.Direction.R);
        rel.setNext(3);
        rel.setB(4);
        elements.add(rel);

        /*
        {
            "eNum": 3,
            "type": "ETyped",
            "eTag": "B",
            "eType": 2
        }
        */

        ETyped eTyped = new ETyped();
        eTyped.seteNum(3);
        eTyped.seteTag("B");
        eTyped.seteType(2);
        elements.add(eTyped);

        /*
        {
            "eNum": 4,
            "type": "HQuant",
            "qType": "all",
            "b": [
                5,
                6
              ]
        }
        */

        HQuant hQuant = new HQuant();
        hQuant.seteNum(4);
        hQuant.setqType(QuantType.all);
        hQuant.setB(Arrays.asList(5,6));
        elements.add(hQuant);

        /*
        {
            "eNum": 5,
            "type": "RelProp",
            "pType": "1",
            "pTag": "1",
            "con": {
                "op": "gt",
                "expr": "1010-01-01T00:00:00.000"
            }
        }
       */

        Constraint conRelProp1 = new Constraint();
        conRelProp1.setOp(ConstraintOp.gt);
        conRelProp1.setExpr("1010-01-01T00:00:00.000");

        RelProp relProp1 = new RelProp();
        relProp1.seteNum(5);
        relProp1.setpType("1");
        relProp1.setpTag("1");
        relProp1.setCon(conRelProp1);
        elements.add(relProp1);

        /*
        {
          "eNum": 6,
          "type": "RelProp",
          "pType": "2",
          "pTag": "2",
          "con": {
            "op": "lt",
            "expr": "10"
          }
        }
        */

        Constraint conRelProp2 = new Constraint();
        conRelProp2.setOp(ConstraintOp.lt);
        conRelProp2.setExpr("10");

        RelProp relProp2 = new RelProp();
        relProp2.seteNum(6);
        relProp2.setpType("2");
        relProp2.setpTag("2");
        relProp2.setCon(conRelProp2);
        elements.add(relProp2);

        query.setElements(elements);


        Supplier<AsgQuery> asgSupplier = new RecTwoPassAsgQuerySupplier(query);
        AsgQuery asgQuery = asgSupplier.get();
        return  asgQuery;
    }
}

