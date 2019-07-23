package com.yangdb.fuse.asg;

import com.google.common.base.Supplier;
import com.yangdb.fuse.dispatcher.asg.AsgQuerySupplier;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.*;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.quant.HQuant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.gt;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static com.yangdb.fuse.model.query.quant.QuantType.all;

/**
 * Created by Roman on 25/04/2017.
 */
public class AsgQueryStore {


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
     */

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
     */

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
        eConcrete.seteType("Dragon");
        eConcrete.seteName("Balerion");
        eConcrete.setNext(2);
        elements.add(eConcrete);

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType("fire");
        rel.setDir(R);
        rel.setNext(3);
        rel.setB(4);
        elements.add(rel);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(3);
        eTyped.seteTag("B");
        eTyped.seteType("Dragon");
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


        Supplier<AsgQuery> asgSupplier = new AsgQuerySupplier(query);
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
        eConcrete.seteType("Dragon");
        eConcrete.seteName("Balerion");
        eConcrete.setNext(2);
        elements.add(eConcrete);

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType("fire");
        rel.setDir(R);
        rel.setNext(3);
        rel.setB(4);
        elements.add(rel);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(3);
        eTyped.seteTag("B");
        eTyped.seteType("Dragon");
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


        Supplier<AsgQuery> asgSupplier = new AsgQuerySupplier(query);
        AsgQuery asgQuery = asgSupplier.get();
        return asgQuery;
    }
}

