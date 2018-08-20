package com.kayhut.fuse.epb.query;

import com.kayhut.fuse.epb.plan.query.AsgUnionSplitQueryTransformer;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.unipop.controller.utils.map.MapBuilder;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.quant.QuantType.*;

public class PermutationVisitorTests {
    @Test
    public void test_1_some_quant_with_1_element() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, some)).in(typed(3, "1")).build();

        AsgUnionSplitQueryTransformer.PermutationVisitor visitor = new AsgUnionSplitQueryTransformer.PermutationVisitor(Collections.emptyMap());
        Set<Map<Integer, Integer>> permutations = visitor.visit(query.getStart());

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(2, 3).get())
                        .toJavaSet(),
                permutations);
    }

    @Test
    public void test_1_some_quant_with_2_elements() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, some)).in(typed(3, "1"), typed(4, "1")).build();

        AsgUnionSplitQueryTransformer.PermutationVisitor visitor = new AsgUnionSplitQueryTransformer.PermutationVisitor(Collections.emptyMap());
        Set<Map<Integer, Integer>> permutations = visitor.visit(query.getStart());

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(2, 3).get(),
                        new MapBuilder<>().put(2, 4).get())
                        .toJavaSet(),
                permutations);
    }

    @Test
    public void test_1_some_quant_with_3_elements() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, some)).in(typed(3, "1"), typed(4, "1"), typed(5, "1")).build();

        AsgUnionSplitQueryTransformer.PermutationVisitor visitor = new AsgUnionSplitQueryTransformer.PermutationVisitor(Collections.emptyMap());
        Set<Map<Integer, Integer>> permutations = visitor.visit(query.getStart());

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(2, 3).get(),
                        new MapBuilder<>().put(2, 4).get(),
                        new MapBuilder<>().put(2, 5).get())
                        .toJavaSet(),
                permutations);
    }

    @Test
    public void test_2_some_quants_with_1_elements() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(quant1(3, some).next(typed(4, "1")),
                    quant1(5, some).next(typed(6, "1")))
                .build();

        AsgUnionSplitQueryTransformer.PermutationVisitor visitor = new AsgUnionSplitQueryTransformer.PermutationVisitor(Collections.emptyMap());
        Set<Map<Integer, Integer>> permutations = visitor.visit(query.getStart());

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(3, 4).put(5, 6).get())
                        .toJavaSet(),
                permutations);
    }

    @Test
    public void test_2_some_quants_with_2_elements() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(quant1(3, some).next(typed(4, "1")).next(typed(5, "1")),
                        quant1(6, some).next(typed(7, "1")).next(typed(8, "1")))
                .build();

        AsgUnionSplitQueryTransformer.PermutationVisitor visitor = new AsgUnionSplitQueryTransformer.PermutationVisitor(Collections.emptyMap());
        Set<Map<Integer, Integer>> permutations = visitor.visit(query.getStart());

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(3, 4).put(6, 7).get(),
                        new MapBuilder<>().put(3, 4).put(6, 8).get(),
                        new MapBuilder<>().put(3, 5).put(6, 7).get(),
                        new MapBuilder<>().put(3, 5).put(6, 8).get())
                        .toJavaSet(),
                permutations);
    }

    @Test
    public void test_2_some_quants_with_1_and_2_elements() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(quant1(3, some).next(typed(4, "1")),
                        quant1(6, some).next(typed(7, "1")).next(typed(8, "1")))
                .build();

        AsgUnionSplitQueryTransformer.PermutationVisitor visitor = new AsgUnionSplitQueryTransformer.PermutationVisitor(Collections.emptyMap());
        Set<Map<Integer, Integer>> permutations = visitor.visit(query.getStart());

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(3, 4).put(6, 7).get(),
                        new MapBuilder<>().put(3, 4).put(6, 8).get())
                        .toJavaSet(),
                permutations);
    }

    @Test
    public void test_2_some_quants_with_2_and_3_elements() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(quant1(3, some).next(typed(4, "1")).next(typed(5, "1")),
                        quant1(6, some).next(typed(7, "1")).next(typed(8, "1")).next(typed(9, "1")))
                .build();

        AsgUnionSplitQueryTransformer.PermutationVisitor visitor = new AsgUnionSplitQueryTransformer.PermutationVisitor(Collections.emptyMap());
        Set<Map<Integer, Integer>> permutations = visitor.visit(query.getStart());

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(3, 4).put(6, 7).get(),
                        new MapBuilder<>().put(3, 4).put(6, 8).get(),
                        new MapBuilder<>().put(3, 4).put(6, 9).get(),
                        new MapBuilder<>().put(3, 5).put(6, 7).get(),
                        new MapBuilder<>().put(3, 5).put(6, 8).get(),
                        new MapBuilder<>().put(3, 5).put(6, 9).get())
                        .toJavaSet(),
                permutations);
    }

    @Test
    public void test_2_some_quants_with_3_elements() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1")).next(quant1(2, all))
                .in(quant1(3, some).next(typed(4, "1")).next(typed(5, "1")).next(typed(6, "1")),
                        quant1(7, some).next(typed(8, "1")).next(typed(9, "1")).next(typed(10, "1")))
                .build();

        AsgUnionSplitQueryTransformer.PermutationVisitor visitor = new AsgUnionSplitQueryTransformer.PermutationVisitor(Collections.emptyMap());
        Set<Map<Integer, Integer>> permutations = visitor.visit(query.getStart());

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(3, 4).put(7, 8).get(),
                        new MapBuilder<>().put(3, 4).put(7, 9).get(),
                        new MapBuilder<>().put(3, 4).put(7, 10).get(),
                        new MapBuilder<>().put(3, 5).put(7, 8).get(),
                        new MapBuilder<>().put(3, 5).put(7, 9).get(),
                        new MapBuilder<>().put(3, 5).put(7, 10).get(),
                        new MapBuilder<>().put(3, 6).put(7, 8).get(),
                        new MapBuilder<>().put(3, 6).put(7, 9).get(),
                        new MapBuilder<>().put(3, 6).put(7, 10).get())
                        .toJavaSet(),
                permutations);
    }

    @Test
    public void test_2_nested_some_quants_with_1_element() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1"))
                .next(quant1(2, some)).next(typed(3, "1")).next(quant1(4, some)).next(typed(5, "1"))
                .build();

        AsgUnionSplitQueryTransformer.PermutationVisitor visitor = new AsgUnionSplitQueryTransformer.PermutationVisitor(Collections.emptyMap());
        Set<Map<Integer, Integer>> permutations = visitor.visit(query.getStart());

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(2, 3).put(4, 5).get())
                        .toJavaSet(),
                permutations);
    }

    @Test
    public void test_2_nested_some_quants_with_2_elements() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1"))
                .next(quant1(2, some))
                .in(typed(3, "1").next(quant1(4, some).next(typed(5, "1")).next(typed(6, "1"))),
                    typed(7, "1").next(quant1(8, some).next(typed(9, "1")).next(typed(10, "1"))))
                .build();

        AsgUnionSplitQueryTransformer.PermutationVisitor visitor = new AsgUnionSplitQueryTransformer.PermutationVisitor(Collections.emptyMap());
        Set<Map<Integer, Integer>> permutations = visitor.visit(query.getStart());

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(2, 3).put(4, 5).get(),
                        new MapBuilder<>().put(2, 3).put(4, 6).get(),
                        new MapBuilder<>().put(2, 7).put(8, 9).get(),
                        new MapBuilder<>().put(2, 7).put(8, 10).get())
                        .toJavaSet(),
                permutations);
    }

    @Test
    public void test_2_nested_some_quants_with_1_and_2_elements() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1"))
                .next(quant1(2, some))
                .in(typed(3, "1").next(quant1(4, some).next(typed(5, "1"))),
                        typed(7, "1").next(quant1(8, some).next(typed(9, "1")).next(typed(10, "1"))))
                .build();

        AsgUnionSplitQueryTransformer.PermutationVisitor visitor = new AsgUnionSplitQueryTransformer.PermutationVisitor(Collections.emptyMap());
        Set<Map<Integer, Integer>> permutations = visitor.visit(query.getStart());

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(2, 3).put(4, 5).get(),
                        new MapBuilder<>().put(2, 7).put(8, 9).get(),
                        new MapBuilder<>().put(2, 7).put(8, 10).get())
                        .toJavaSet(),
                permutations);
    }

    @Test
    public void test_2_nested_some_quants_with_3_elements() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1"))
                .next(quant1(2, some))
                .in(typed(3, "1").next(quant1(4, some).next(typed(5, "1")).next(typed(6, "1")).next(typed(7, "1"))),
                    typed(8, "1").next(quant1(9, some).next(typed(10, "1")).next(typed(11, "1")).next(typed(12, "1"))))
                .build();

        AsgUnionSplitQueryTransformer.PermutationVisitor visitor = new AsgUnionSplitQueryTransformer.PermutationVisitor(Collections.emptyMap());
        Set<Map<Integer, Integer>> permutations = visitor.visit(query.getStart());

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(2, 3).put(4, 5).get(),
                        new MapBuilder<>().put(2, 3).put(4, 6).get(),
                        new MapBuilder<>().put(2, 3).put(4, 7).get(),
                        new MapBuilder<>().put(2, 8).put(9, 10).get(),
                        new MapBuilder<>().put(2, 8).put(9, 11).get(),
                        new MapBuilder<>().put(2, 8).put(9, 12).get())
                        .toJavaSet(),
                permutations);
    }

    @Test
    public void test_2_nested_some_quants_with_2_and_3_elements() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1"))
                .next(quant1(2, some))
                .in(typed(3, "1").next(quant1(4, some).next(typed(5, "1")).next(typed(6, "1"))),
                        typed(8, "1").next(quant1(9, some).next(typed(10, "1")).next(typed(11, "1")).next(typed(12, "1"))))
                .build();

        AsgUnionSplitQueryTransformer.PermutationVisitor visitor = new AsgUnionSplitQueryTransformer.PermutationVisitor(Collections.emptyMap());
        Set<Map<Integer, Integer>> permutations = visitor.visit(query.getStart());

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(2, 3).put(4, 5).get(),
                        new MapBuilder<>().put(2, 3).put(4, 6).get(),
                        new MapBuilder<>().put(2, 8).put(9, 10).get(),
                        new MapBuilder<>().put(2, 8).put(9, 11).get(),
                        new MapBuilder<>().put(2, 8).put(9, 12).get())
                        .toJavaSet(),
                permutations);
    }

    @Test
    public void test_2_nested_some_quants_whith_all_quant_with_3_elements() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1"))
                .next(quant1(2, some))
                .in(typed(3, "1").next(quant1(4, all).next(typed(5, "1")).next(typed(6, "1")).next(typed(7, "1"))),
                        typed(8, "1").next(quant1(9, some).next(typed(10, "1")).next(typed(11, "1")).next(typed(12, "1"))))
                .build();

        AsgUnionSplitQueryTransformer.PermutationVisitor visitor = new AsgUnionSplitQueryTransformer.PermutationVisitor(Collections.emptyMap());
        Set<Map<Integer, Integer>> permutations = visitor.visit(query.getStart());

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(2, 3).get(),
                        new MapBuilder<>().put(2, 8).put(9, 10).get(),
                        new MapBuilder<>().put(2, 8).put(9, 11).get(),
                        new MapBuilder<>().put(2, 8).put(9, 12).get())
                        .toJavaSet(),
                permutations);
    }

    @Test
    public void test_mix() {
        AsgQuery query = start("q1", "ont").next(typed(1, "1"))
                .next(quant1(2, some))
                .in(quant1(3, all).next(quant1(4, some).next(typed(5, "1")).next(typed(6, "1"))),
                    typed(7, "1"),
                    quant1(8, some).next(quant1(9, all).next(typed(10, "1")).next(typed(11, "1")))
                                   .next(typed(12, "1")))
                .build();

        AsgUnionSplitQueryTransformer.PermutationVisitor visitor = new AsgUnionSplitQueryTransformer.PermutationVisitor(Collections.emptyMap());
        Set<Map<Integer, Integer>> permutations = visitor.visit(query.getStart());

        Assert.assertEquals(
                Stream.of(
                        new MapBuilder<>().put(2, 3).put(4, 5).get(),
                        new MapBuilder<>().put(2, 3).put(4, 6).get(),
                        new MapBuilder<>().put(2, 7).get(),
                        new MapBuilder<>().put(2, 8).put(8, 9).get(),
                        new MapBuilder<>().put(2, 8).put(8, 12).get())
                        .toJavaSet(),
                permutations);
    }
}
