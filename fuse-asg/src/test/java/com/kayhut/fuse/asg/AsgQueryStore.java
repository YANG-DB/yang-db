package com.kayhut.fuse.asg;

import com.google.common.base.Supplier;
import com.kayhut.fuse.asg.builder.RecTwoPassAsgQuerySupplier;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
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

    /**

     +----+       +---------+
     |S(0)| +--+  |eTyped(1)|
     +----+       +---------+

     * @param queryName
     * @param ontologyName
     * @return
     */
    public static AsgQuery simpleQuery0(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType(1);

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped).build())
                .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    /**

     +----+       +---------+               +---------+
     |S(0)| +--+  |eTyped(1)| +--rel(2)+--> |eTyped(3)|
     +----+       +---------+               +---------+

     * @param queryName
     * @param ontologyName
     * @return
     */
    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
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

    /**
                                                            +----+
                                                            |    |               +-------------+
                                                            |    | +-----------+ |ePropGroup(9)|
                                                            |    |               +-------------+
                                                            |    |
     +----+       +---------+              +---------+      |    |               +---------+
     |S(0)| +--+  |eTyped(1)| +-+rel(2)+-> |eTyped(3)| +--+ |&(4)| +-+rel(5)+--> |eTyped(6)|
     +----+       +---------+       +      +---------+      |    |               +---------+
                                    |                       |    |
                                    |                       |    |               +---------+
                            +-------+--------+              |    | +-+rel(7)+--> |eTyped(8)|
                            |relPropGroup(10)|              |    |       +       +---------+
                            +----------------+              +----+       |
                                                                         |
                                                                +--------+-------+
                                                                |relPropGroup(11)|
                                                                +----------------+
     * @param queryName
     * @param ontologyName
     * @return
     */
    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
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

        Constraint constraint1 = new Constraint();
        constraint1.setOp(ConstraintOp.eq);
        constraint1.setExpr("value1");

        EProp prop1 = new EProp();
        prop1.setpType("1");
        prop1.setCon(constraint1);
        prop1.seteNum(9);

        Constraint constraint2 = new Constraint();
        constraint2.setOp(ConstraintOp.eq);
        constraint2.setExpr(30);

        EProp prop2 = new EProp();
        prop2.setpType("2");
        prop2.setCon(constraint2);
        prop2.seteNum(10);

        EPropGroup propGroup = new EPropGroup();
        propGroup.seteNum(9);
        propGroup.seteProps(Arrays.asList(prop1, prop2));

        Constraint constraint3 = new Constraint();
        constraint3.setOp(ConstraintOp.gt);
        constraint3.setExpr(10);
        RelProp relProp1 = new RelProp();
        relProp1.setCon(constraint3);
        relProp1.setpType("2");
        RelPropGroup relPropGroup1 = new RelPropGroup();
        relPropGroup1.seteNum(10);
        relPropGroup1.setrProps(Arrays.asList(relProp1));

        Constraint constraint4 = new Constraint();
        constraint4.setOp(ConstraintOp.lt);
        constraint4.setExpr(20);
        RelProp relProp2 = new RelProp();
        relProp2.setCon(constraint4);
        relProp2.setpType("2");
        RelPropGroup relPropGroup2 = new RelPropGroup();
        relPropGroup2.seteNum(11);
        relPropGroup2.setrProps(Arrays.asList(relProp2));

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
                                                                .withB(AsgEBase.Builder.get().withEBase(relPropGroup2).build())
                                                        .build())
                                                        .withNext(AsgEBase.Builder.get().withEBase(propGroup).build())
                                                        .build())
                                                .build())
                                        .withB(AsgEBase.Builder.get().withEBase(relPropGroup1).build())
                                        .build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    public static AsgQuery Q1(){
        //region Query Building
        Query query = new Query(); //Person owns Dragon with EProp - Name: 'dragonA'
        query.setOnt("Dragons");
        query.setName("Q1");
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

        /* Person
         {
          "eNum": 1,
          "type": "ETyped",
          "eTag": "A",
          "eType": 1,
          "next":2
        }
        */

        ETyped eTypedA = new ETyped();
        eTypedA.seteNum(1);
        eTypedA.seteTag("A");
        eTypedA.seteType(1);
        eTypedA.setNext(2);
        elements.add(eTypedA);

        /* Owns
        {
          "eNum": 2,
          "type": "Rel",
          "rType": 1,
          "dir": "R",
          "next": 3
        }
         */
        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType(1);
        rel.setDir(Rel.Direction.R);
        rel.setNext(3);
        elements.add(rel);


        /* Dragon
        {
          "eNum": 3,
          "type": "ETyped",
          "eTag": "B",
          "eType": 2
        }
        */
        ETyped eTypedB = new ETyped();
        eTypedB.seteNum(3);
        eTypedB.seteTag("B");
        eTypedB.seteType(2);
        eTypedB.setNext(4);
        elements.add(eTypedB);

        /* The dragon has the Name Entity Property = "dragonA"
            "type": "EProp",
            "eNum": 4,
            "pType": "1.1",
            "pTag": "1",
            "con": {
            "op": "eq",
            "expr": "dragonA"
            }
         */


        EProp eProp = new EProp();
        eProp.seteNum(4);
        eProp.setpType("1");
        eProp.setpTag("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        con.setExpr("dragonA");
        eProp.setCon(con);
        elements.add(eProp);

        query.setElements(elements);

        //endregion

        Supplier<AsgQuery> asgSupplier = new RecTwoPassAsgQuerySupplier(query);
        AsgQuery asgQuery = asgSupplier.get();
        return  asgQuery;
    }

    public static AsgQuery Q188_V1(){
        //region Query Building
        Query query = new Query(); //Person owns Dragon with EProp - Name: 'dragonA'
        query.setOnt("Dragons");
        query.setName("Q188");
        List<EBase> elements = new ArrayList<EBase>();

        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);

        EConcrete eConcrete = new EConcrete();
        eConcrete.seteNum(1);
        eConcrete.seteTag("A");
        eConcrete.seteID("1234");
        eConcrete.seteType(2);
        eConcrete.seteName("Balerion");
        eConcrete.setNext(2);
        elements.add(eConcrete);

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType(3);
        rel.setDir(Rel.Direction.R);
        rel.setNext(3);
        rel.setB(4);
        elements.add(rel);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(3);
        eTyped.seteTag("B");
        eTyped.seteType(2);
        elements.add(eTyped);

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

        Start start  = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);

        EConcrete eConcrete = new EConcrete();
        eConcrete.seteNum(1);
        eConcrete.seteTag("A");
        eConcrete.seteID("1234");
        eConcrete.seteType(2);
        eConcrete.seteName("Balerion");
        eConcrete.setNext(2);
        elements.add(eConcrete);

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType(3);
        rel.setDir(Rel.Direction.R);
        rel.setNext(3);
        rel.setB(4);
        elements.add(rel);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(3);
        eTyped.seteTag("B");
        eTyped.seteType(2);
        elements.add(eTyped);

        HQuant hQuant = new HQuant();
        hQuant.seteNum(4);
        hQuant.setqType(QuantType.all);
        hQuant.setB(Arrays.asList(5,6));
        elements.add(hQuant);

        Constraint conRelProp1 = new Constraint();
        conRelProp1.setOp(ConstraintOp.gt);
        conRelProp1.setExpr("1010-01-01T00:00:00.000");

        RelProp relProp1 = new RelProp();
        relProp1.seteNum(5);
        relProp1.setpType("1");
        relProp1.setpTag("1");
        relProp1.setCon(conRelProp1);
        elements.add(relProp1);

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

