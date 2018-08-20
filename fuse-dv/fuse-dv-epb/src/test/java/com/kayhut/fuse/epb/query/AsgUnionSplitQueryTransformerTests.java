package com.kayhut.fuse.epb.query;

import com.kayhut.fuse.epb.plan.query.AsgUnionSplitQueryTransformer;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.quant1;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.start;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.typed;
import static com.kayhut.fuse.model.query.quant.QuantType.some;

/**
 * Created by Roman on 8/19/2018.
 */
public class AsgUnionSplitQueryTransformerTests {
    @Test
    public void test_1_some_quant_with_1_element() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, some)).in(typed(3, "1")).build();

        Iterable<AsgQuery> new AsgUnionSplitQueryTransformer().transform(query);

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(2, 3).get())
                        .toJavaSet(),
                permutations);
    }
}
