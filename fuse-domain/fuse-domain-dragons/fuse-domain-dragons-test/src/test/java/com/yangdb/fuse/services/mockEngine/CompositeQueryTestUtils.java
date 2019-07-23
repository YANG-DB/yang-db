package com.yangdb.fuse.services.mockEngine;

import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.properties.constraint.InnerQueryConstraint;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;

import java.util.Arrays;

import static com.yangdb.fuse.model.OntologyTestUtils.OWN;

public abstract class CompositeQueryTestUtils {
    public static Query Q0() {
        Query query = Query.Builder.instance().withName("q0").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "id",InnerQueryConstraint.of(ConstraintOp.inSet,Q1(),"P","id")))
                )).build();
        return query;
    }

    public static Query Q1() {
        Query query = Query.Builder.instance().withName("q1").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "id",InnerQueryConstraint.of(ConstraintOp.inSet,Q2(),"P","id")))
                )).build();
        return query;
    }

    public static Query Q2() {
        Query query = Query.Builder.instance().withName("q2").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "name",Constraint.of(ConstraintOp.like,"jhon*")))
                )).build();
        return query;
    }

    public static Query Q3() {
        Query query = Query.Builder.instance().withName("q3").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "name",Constraint.of(ConstraintOp.inSet,Arrays.asList("jhon","george","jim"))))
                )).build();
        return query;
    }

    public static Query Q4() {
        Query query = Query.Builder.instance().withName("q4").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(4,8),0),
                        new Rel(4, OWN.getrType(), Rel.Direction.R, null, 5, 0),
                        new ETyped(5, "C", OntologyTestUtils.DRAGON.name, 6, 0),
                        new EPropGroup(6,
                                new EProp(7, "id",InnerQueryConstraint.of(ConstraintOp.inSet,Q2(),"P","id"))),

                        new Rel(8, OWN.getName(), Rel.Direction.R, null, 9, 0),
                        new ETyped(9, "D", OntologyTestUtils.DRAGON.name, 10, 0),
                        new EProp(10, "id",InnerQueryConstraint.of(ConstraintOp.inSet,Q3(),"P","id"))
                )).build();
        return query;
    }

}
