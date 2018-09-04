package com.kayhut.fuse.epb.plan.validation.opValidator;

import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityJoinOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import org.junit.Assert;
import org.junit.Test;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

public class StraightPathJoinOpValidatorTests {
    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        Start start = new Start();
        start.seteNum(0);

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("A");
        eTyped.seteType("1");

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(R);
        rel.setrType("1");

        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(3);
        eTyped2.seteTag("B");
        eTyped2.seteType("2");

        Rel rel2 = new Rel();
        rel2.seteNum(4);
        rel2.setDir(R);
        rel2.setrType("1");

        ETyped eTyped3 = new ETyped();
        eTyped3.seteNum(5);
        eTyped3.seteTag("C");
        eTyped3.seteType("2");

        AsgEBase<Start> asgStart =
                AsgEBase.Builder.<Start>get().withEBase(start)
                        .withNext(AsgEBase.Builder.get().withEBase(eTyped)
                                .withNext(AsgEBase.Builder.get().withEBase(rel)
                                        .withNext(AsgEBase.Builder.get().withEBase(eTyped2)
                                                .withNext(AsgEBase.Builder.get().withEBase(rel2)
                                                        .withNext(AsgEBase.Builder.get().withEBase(eTyped3).build())
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build();

        return AsgQuery.AsgQueryBuilder.anAsgQuery().withName(queryName).withOnt(ontologyName).withStart(asgStart).build();
    }

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "1", "A"))
                .next(rel(2, "1", R).below(relProp(10, RelProp.of(10, "2", of(eq, "value2")))))
                .next(typed(3, "2", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(9, EProp.of(9, "1", of(eq, "value1")), EProp.of(9, "3", of(gt, "value3")))
                        , rel(5, "4", R)
                                .next(unTyped(6, "C"))
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of(11, "5", of(eq, "value5")), RelProp.of(11, "4", of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D"))
                )
                .build();
    }

    @Test
    public void simplePathValidTest(){
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan left = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        Plan right = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)), new RelationOp(AsgQueryUtil.element$(asgQuery,2)), new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));
        EntityJoinOp join = new EntityJoinOp(left, right);

        StraightPathJoinOpValidator validator = new StraightPathJoinOpValidator();
        Assert.assertTrue(validator.isPlanOpValid(asgQuery, new Plan(join), 0).valid());

    }

    @Test
    public void simplePathValidTest2(){
        AsgQuery asgQuery = simpleQuery1("name", "ont");
        Plan left = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 3)));

        Plan right = new Plan(new EntityOp(AsgQueryUtil.element$(asgQuery, 1)));
        EntityJoinOp join = new EntityJoinOp(left, right);

        StraightPathJoinOpValidator validator = new StraightPathJoinOpValidator();
        Assert.assertTrue(validator.isPlanOpValid(asgQuery, new Plan(join), 0).valid());

    }

    @Test
    public void notValidPathTest(){
        AsgQuery query = simpleQuery2("q", "ont");
        Plan left = new Plan(new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)),
                new RelationOp(AsgQueryUtil.element$(query, 5)),
                new EntityOp(AsgQueryUtil.element$(query, 6)));
        Plan right = new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)));
        EntityJoinOp join = new EntityJoinOp(left, right);
        StraightPathJoinOpValidator validator = new StraightPathJoinOpValidator();
        Assert.assertFalse(validator.isPlanOpValid(query, new Plan(join), 0).valid());
    }

    @Test
    public void validPathWithPropsTest(){
        AsgQuery query = simpleQuery2("q", "ont");
        Plan left = new Plan(new EntityOp(AsgQueryUtil.element$(query, 3)),
                new EntityFilterOp(AsgQueryUtil.element$(query, 9)));
        Plan right = new Plan(new EntityOp(AsgQueryUtil.element$(query, 1)));
        EntityJoinOp join = new EntityJoinOp(left, right);
        StraightPathJoinOpValidator validator = new StraightPathJoinOpValidator();
        Assert.assertTrue(validator.isPlanOpValid(query, new Plan(join), 0).valid());
    }
}
