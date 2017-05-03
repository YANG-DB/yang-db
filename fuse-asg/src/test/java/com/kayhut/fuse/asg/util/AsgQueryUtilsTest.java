package com.kayhut.fuse.asg.util;

import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryAssert;
import com.kayhut.fuse.model.query.EBase;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Roman on 25/04/2017.
 */
public class AsgQueryUtilsTest {
    //region Test Methods
    @Test
    public void testFindPath_AdjacentEntities_1_2_3() {
        AsgQuery query = AsgQueryStore.simpleQuery1("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(1, 2, 3));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtils.getPath(query, 1, 3);
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testFindPath_AdjacentEntities_3_2_1() {
        AsgQuery query = AsgQueryStore.simpleQuery1("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(3, 2, 1));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtils.getPath(query, 3, 1);
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testFindPath_AdjacentEntities_3_4_5_6() {
        AsgQuery query = AsgQueryStore.simpleQuery2("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(3, 4, 5, 6));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtils.getPath(query, 3, 6);
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testFindPath_AdjacentEntities_6_5_4_3() {
        AsgQuery query = AsgQueryStore.simpleQuery2("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(6, 5, 4, 3));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtils.getPath(query, 6, 3);
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testFindPath_AdjacentEntities_3_4_7_8() {
        AsgQuery query = AsgQueryStore.simpleQuery2("name", "ont");

        List<AsgEBase<? extends EBase>> expectedPath = getExpectedPath(query, Arrays.asList(3, 4, 7, 8));
        List<AsgEBase<? extends EBase>> actualPath = AsgQueryUtils.getPath(query, 3, 8);
        assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testFindPath_AdjacentEntities_8_7_4_3() {
        AsgQuery query = AsgQueryStore.simpleQuery2("name", "ont");

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
