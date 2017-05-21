package com.kayhut.fuse.asg.util;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryAssert;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * Created by Roman on 25/04/2017.
 */
public class AsgQueryUtilsTest {
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

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "A", 1))
                .next(rel(R, 2, 1).below(relProp(10, RelProp.of("2", 10, of(eq, "value2")))))
                .next(typed(2, "B", 3))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of("1", 9, of(eq, "value1")), EProp.of("3", 9, of(gt, "value3")))
                        , rel(R, 5, 4)
                                .next(unTyped("C", 6))
                        , rel(R, 7, 5)
                                .below(relProp(11, RelProp.of("5", 11, of(eq, "value5")), RelProp.of("4", 11, of(eq, "value4"))))
                                .next(concrete("concrete1", 3, "Concrete1", "D", 8))
                )
                .build();
    }

    //region Test Methods
    @Test
    public void testFindPath_AdjacentEntities_1_2_3() {
        AsgQuery query = simpleQuery1("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(1, 2, 3));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtils.getPath(query, 1, 3);
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testFindPath_AdjacentEntities_3_2_1() {
        AsgQuery query = simpleQuery1("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(3, 2, 1));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtils.getPath(query, 3, 1);
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testFindPath_AdjacentEntities_3_4_5_6() {
        AsgQuery query = simpleQuery2("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(3, 4, 5, 6));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtils.getPath(query, 3, 6);
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testFindPath_AdjacentEntities_6_5_4_3() {
        AsgQuery query = simpleQuery2("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(6, 5, 4, 3));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtils.getPath(query, 6, 3);
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testFindPath_AdjacentEntities_3_4_7_8() {
        AsgQuery query = simpleQuery2("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(3, 4, 7, 8));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtils.getPath(query, 3, 8);
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testFindPath_AdjacentEntities_8_7_4_3() {
        AsgQuery query = simpleQuery2("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(8, 7, 4, 3));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtils.getPath(query, 8, 3);
        assertEquals(expectedPath, actualPath);
    }
    //endregion

    //region Private Methods
    private List<AsgEBase<? extends EBase>> getExpectedPath(AsgQuery query, Iterable<Integer> eNums) {
        List<AsgEBase<? extends EBase>> expectedPath = new ArrayList<>();
        for(int eNum : eNums) {
            expectedPath.add(AsgQueryUtils.getNextDescendant(query.getStart(), eNum).get());
        }
        return expectedPath;
    }

    private void assertEquals(List<AsgEBase<? extends EBase>> expectedPath, List<AsgEBase<? extends EBase>> actualPath) {
        if (expectedPath == null) {
            Assert.assertTrue(actualPath == null);
        }

        Assert.assertTrue(expectedPath != null && actualPath != null);
        Assert.assertEquals(expectedPath.size(), actualPath.size());

        for(int i = 0 ; i < expectedPath.size() ; i++) {
            AsgEBase<? extends EBase> expectedAsgEBase = expectedPath.get(i);
            AsgEBase<? extends EBase> actualAsgEBase = actualPath.get(i);

            AsgQueryAssert.assertEquals(expectedAsgEBase, actualAsgEBase);
        }

    }
    //endregion
}
