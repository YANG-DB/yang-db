package com.kayhut.fuse.asg.util;

import com.kayhut.fuse.dispatcher.descriptors.AsgQueryDescriptor;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryAssert;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.kayhut.fuse.model.OntologyTestUtils.OWN;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

/**
 * Created by Roman on 25/04/2017.
 */
public class AsgQueryUtilTest {
    public static AsgQuery simpleQuery1(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, OntologyTestUtils.PERSON.type,"A"))
                .next(rel(2,OWN.getrType(),R))
                .next(typed(3, OntologyTestUtils.DRAGON.type,"B")).build();
    }

    public static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "1", "A"))
                .next(rel(2, "1", R).below(relProp(10, RelProp.of("2", 10, of(eq, "value2")))))
                .next(typed(3, "2", "B"))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of("1", 9, of(eq, "value1")), EProp.of("3", 9, of(gt, "value3")))
                        , rel(5, "4", R)
                                .next(unTyped(6, "C"))
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of("5", 11, of(eq, "value5")), RelProp.of("4", 11, of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D"))
                )
                .build();
    }

    //region Test Methods
    @Test
    public void testFindPath_AdjacentEntities_1_2_3() {
        AsgQuery query = simpleQuery1("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(1, 2, 3));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtil.path(query, 1, 3);
        assertEquals(expectedPath, actualPath);
        System.out.println(AsgQueryDescriptor.print(query));
    }

    @Test
    public void testFindPath_AdjacentEntities_3_2_1() {
        AsgQuery query = simpleQuery1("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(3, 2, 1));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtil.path(query, 3, 1);
        assertEquals(expectedPath, actualPath);
        System.out.println(AsgQueryDescriptor.print(query));
    }

    @Test
    public void testFindPath_AdjacentEntities_3_4_5_6() {
        AsgQuery query = simpleQuery2("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(3, 4, 5, 6));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtil.path(query, 3, 6);
        assertEquals(expectedPath, actualPath);
        System.out.println(AsgQueryDescriptor.print(query));
    }

    @Test
    public void testFindPath_AdjacentEntities_6_5_4_3() {
        AsgQuery query = simpleQuery2("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(6, 5, 4, 3));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtil.path(query, 6, 3);
        assertEquals(expectedPath, actualPath);
        System.out.println(AsgQueryDescriptor.print(query));
    }

    @Test
    public void testFindPath_AdjacentEntities_3_4_7_8() {
        AsgQuery query = simpleQuery2("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(3, 4, 7, 8));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtil.path(query, 3, 8);
        assertEquals(expectedPath, actualPath);
        System.out.println(AsgQueryDescriptor.print(query));
    }

    @Test
    public void testFindPath_AdjacentEntities_8_7_4_3() {
        AsgQuery query = simpleQuery2("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(8, 7, 4, 3));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtil.path(query, 8, 3);
        assertEquals(expectedPath, actualPath);
        System.out.println(AsgQueryDescriptor.print(query));
    }
    //endregion

    //region Private Methods
    private List<AsgEBase<? extends EBase>> getExpectedPath(AsgQuery query, Iterable<Integer> eNums) {
        List<AsgEBase<? extends EBase>> expectedPath = new ArrayList<>();
        for(int eNum : eNums) {
            expectedPath.add(AsgQueryUtil.nextDescendant(query.getStart(), eNum).get());
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
