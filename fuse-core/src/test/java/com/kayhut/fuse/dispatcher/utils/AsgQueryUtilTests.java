package com.kayhut.fuse.dispatcher.utils;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.*;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

public class AsgQueryUtilTests {
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
                .in(eProp(9, EProp.of(9, "1", of(eq, "value1")), EProp.of(9, "3", of(gt, "value3")))
                        , rel(5, "4", R)
                                .next(unTyped(6, "C"))
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of(11, "5", of(eq, "value5")), RelProp.of(11, "4", of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D"))
                )
                .build();
    }

    @Test
    public void nextDescendantsSingleHopSimpleTest(){
        AsgQuery query = simpleQuery1("query", "ont");
        AsgEBase<EBase> e1 = element$(query, 1);
        List<AsgEBase<EBase>> descendantsSingleHop = nextDescendantsSingleHop(e1, EEntityBase.class);
        Assert.assertEquals(1, descendantsSingleHop.size());
        Assert.assertTrue(descendantsSingleHop.get(0).geteBase() instanceof EEntityBase);
        Assert.assertEquals(3,descendantsSingleHop.get(0).geteNum());
    }

    @Test
    public void nextDescendantsSingleHopQuantTest(){
        AsgQuery query = simpleQuery2("q","ont");
        AsgEBase<EBase> e3 = element$(query, 3);
        List<AsgEBase<EBase>> descendantsSingleHop = nextDescendantsSingleHop(e3, EEntityBase.class);
        Assert.assertEquals(2, descendantsSingleHop.size());
        Assert.assertTrue(descendantsSingleHop.stream().anyMatch(e -> e.geteNum() == 6));
        Assert.assertTrue(descendantsSingleHop.stream().anyMatch(e -> e.geteNum() == 8));
    }

    @Test
    public void testAncestorSingleHop(){
        AsgQuery query = simpleQuery2("q","ont");
        AsgEBase<EBase> e3 = element$(query, 6);
        Optional<AsgEBase<EBase>> ancestor = ancestor(e3, EEntityBase.class);
        Assert.assertTrue(ancestor.isPresent());
        Assert.assertEquals(3, ancestor.get().geteNum());
    }
}
