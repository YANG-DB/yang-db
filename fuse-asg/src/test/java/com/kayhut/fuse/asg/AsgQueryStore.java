package com.kayhut.fuse.asg;

import com.google.common.base.Supplier;
import com.kayhut.fuse.asg.builder.RecTwoPassAsgQuerySupplier;
import com.kayhut.fuse.model.OntologyTest;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.OntologyTestUtils.PERSON;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.HQuant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * Created by Roman on 25/04/2017.
 */
public class AsgQueryStore {

    public static AsgQuery Q1() {
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

        Start start = new Start();
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
        return asgQuery;
    }

    /**
     * +----+       +---------+
     * |S(0)| +--+  |eTyped(1)|
     * +----+       +---------+
     *
     * @param queryName
     * @param ontologyName
     * @return
     */

    /**
     * +----+       +---------+               +---------+
     * |S(0)| +--+  |eTyped(1)| +--rel(2)+--> |eTyped(3)|
     * +----+       +---------+               +---------+
     *
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
        rel.setDir(R);
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
     * +----+
     * |    |               +-------------+
     * |    | +-----------+ |ePropGroup(9)|
     * |    |               +-------------+
     * |    |
     * +----+       +---------+              +---------+      |    |               +---------+
     * |S(0)| +--+  |eTyped(1)| +-+rel(2)+-> |eTyped(3)| +--+ |&(4)| +-+rel(5)+--> |eTyped(6)|
     * +----+       +---------+       +      +---------+      |    |               +---------+
     * |                       |    |
     * |                       |    |               +---------+
     * +-------+--------+              |    | +-+rel(7)+--> |eTyped(8)|
     * |relPropGroup(10)|              |    |       +       +---------+
     * +----------------+              +----+       |
     * |
     * +--------+-------+
     * |relPropGroup(11)|
     * +----------------+
     *
     * @param queryName
     * @param ontologyName
     * @return
     */

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1,  1))
                .next(rel(R, 2, 1).below(relProp(10, RelProp.of("2", 10, of(eq, "value2")))))
                .next(typed(2,  3))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of("1", 9, of(eq, "value1")), EProp.of("3", 9, of(gt, "value3")))
                        , rel(R, 5, 4)
                                .next(unTyped( 6))
                        , rel(R, 7, 5)
                                .below(relProp(11, RelProp.of("5", 11, of(eq, "value5")), RelProp.of("4", 11, of(eq, "value4"))))
                                .next(concrete("concrete1", 3, "Concrete1", "D", 8))
                )
                .build();
    }

    /**
     * +----+
     * |    |               +-------------+
     * |    | +-----------+ |ePropGroup(9)|
     * |    |               +-------------+
     * |    |
     * +----+       +---------+              +---------+      |    |               +---------+               +----------+
     * |S(0)| +--+  |eTyped(1)| +-+rel(2)+-> |eTyped(3)| +--+ |&(4)| +-+rel(5)+--> |eTyped(6)| +-+rel(12)+-->|eTyped(13)|
     * +----+       +---------+       +      +---------+      |    |               +---------+               +----------+
     * |                       |    |
     * |                       |    |               +---------+               +----------+
     * +-------+--------+              |    | +-+rel(7)+--> |eTyped(8)| +-+rel(14)+-->|eTyped(15)|
     * |relPropGroup(10)|              |    |       +       +---------+               +----------+
     * +----------------+              +----+       |
     * |
     * +--------+-------+
     * |relPropGroup(11)|
     * +----------------+
     *
     * @param queryName
     * @param ontologyName
     * @return
     */
    public static AsgQuery simpleQuery3(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, 1))
                .next(rel(R, 2, 1).below(relProp(10, RelProp.of("2", 10, of(eq, "value2")))))
                .next(typed(2, 3))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of("1", 9, of(eq, "value1")), EProp.of("3", 9, of(gt, "value3")))
                        , rel(R, 5, 4)
                                .next(unTyped( 6)
                                        .next(rel(R, 12, 4)
                                                .next(typed(4, 13))
                                        )
                                )
                        , rel(R, 7, 5)
                                .below(relProp(11, RelProp.of("5", 11, of(eq, "value5")), RelProp.of("4", 11, of(eq, "value4"))))
                                .next(concrete("concrete1", 3, "Concrete1", "D", 8)
                                        .next(rel(R, 14, 1)
                                                .next(typed(1, 15))
                                        )
                                )
                )
                .build();
    }

    public static AsgQuery queryWithEtypedAndEconcrete(String queryName, String ontologyName){
        return AsgQuery.Builder.start(queryName, ontologyName )
                .next(typed(1, 1))
                .next(rel(R, 2, 1).below(relProp(10, RelProp.of("2", 10, of(eq, "value2")))))
                .next(concrete("123", 2, "B", "tag",3))
                .build();
    }


    public static AsgQuery Q188_V1() {
        //region Query Building
        Query query = new Query(); //Person owns Dragon with EProp - Name: 'dragonA'
        query.setOnt("Dragons");
        query.setName("Q188");
        List<EBase> elements = new ArrayList<EBase>();

        Start start = new Start();
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
        rel.setDir(R);
        rel.setNext(3);
        rel.setB(4);
        elements.add(rel);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(3);
        eTyped.seteTag("B");
        eTyped.seteType(2);
        elements.add(eTyped);

        Constraint conRelProp1 = new Constraint();
        conRelProp1.setOp(gt);
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
        return asgQuery;
    }

    public static AsgQuery Q187_V1() {
        //region Query Building
        Query query = new Query(); //Person owns Dragon with EProp - Name: 'dragonA'
        query.setOnt("Dragons");
        query.setName("Q187");
        List<EBase> elements = new ArrayList<EBase>();

        Start start = new Start();
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
        rel.setDir(R);
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
        hQuant.setqType(all);
        hQuant.setB(Arrays.asList(5, 6));
        elements.add(hQuant);

        Constraint conRelProp1 = new Constraint();
        conRelProp1.setOp(gt);
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
        return asgQuery;
    }
}

