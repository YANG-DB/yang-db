package com.yangdb.fuse.model;

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryAssert;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.ontology.Value;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.yangdb.fuse.model.OntologyTestUtils.*;
import static com.yangdb.fuse.model.OntologyTestUtils.FIRE;
import static com.yangdb.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.yangdb.fuse.model.OntologyTestUtils.NAME;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.ePropGroup;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static com.yangdb.fuse.model.query.properties.RelProp.of;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.gt;
import static com.yangdb.fuse.model.query.quant.QuantType.all;

public class AsgQueryUtilTests {

    public static AsgQuery singleOptional(){
        return AsgQuery.Builder.start("q", "O")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(AsgQuery.Builder.ePropGroup(2,EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                .next(rel(4, OWN.getrType(), R)
                .below(relProp(5, of(6, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(7, OntologyTestUtils.DRAGON.type))
                .next(quant1(8, all))
                .in(AsgQuery.Builder.ePropGroup(9, EProp.of(10, NAME.type, Constraint.of(eq, "smith")), EProp.of(11, GENDER.type, Constraint.of(gt, new Value(MALE.ordinal(),MALE.name()))))
                        , optional(50).next(rel(12, FREEZE.getrType(), R)
                                .next(unTyped(13)
                                        .next(AsgQuery.Builder.ePropGroup(14,EProp.of(15, NAME.type, Constraint.of(ConstraintOp.notContains, "bob"))))
                                ))
                        , rel(16, FIRE.getrType(), R)
                                .next(concrete(20, "smoge", DRAGON.type, "Display:smoge", "D")
                                        .next(AsgQuery.Builder.ePropGroup(21,EProp.of(22, NAME.type, Constraint.of(ConstraintOp.eq, "smoge"))))
                                )
                )
                .build();
    }

    public static AsgQuery singleHierarchicalOptional(){
        return AsgQuery.Builder.start("q", "O")
                .next(typed(1, "entity1", "A"))
                .next(rel(2, "rel1", R,"R").below(relProp(2, RelProp.of(2, "2", Constraint.of(eq, "value2")))))
                .next(typed(3, "entity2", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(5, EProp.of(5, "prop1", Constraint.of(eq, "value1")), EProp.of(5, "prop2", Constraint.of(gt, "value3"))),
                        rel(6, "rel2", R,"R1").next(typed(7, "entity3", "C")),
                        optional(11).next(rel(12, "rel4", R,"R2").next(typed(13, "entity4", "E")
                                .next(optional(14).next(rel(15, "rel4", R).next(typed(16, "entity4", "F")))))))
                .build();
    }

    public static AsgQuery twoOptionals(){
        return AsgQuery.Builder.start("q", "O")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(AsgQuery.Builder.ePropGroup(2,EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                .next(rel(4, OWN.getrType(), R)
                        .below(relProp(5, of(6, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(7, OntologyTestUtils.DRAGON.type))
                .next(quant1(8, all))
                .in(AsgQuery.Builder.ePropGroup(9, EProp.of(10, NAME.type, Constraint.of(eq, "smith")), EProp.of(11, GENDER.type, Constraint.of(gt, new Value(MALE.ordinal(),MALE.name()))))
                        , optional(50).next(rel(12, FREEZE.getrType(), R)
                                .next(unTyped(13)
                                        .next(AsgQuery.Builder.ePropGroup(14,EProp.of(15, NAME.type, Constraint.of(ConstraintOp.notContains, "bob"))))
                                ))
                        , optional(60).next(rel(61, FREEZE.getrType(), R)
                                .next(unTyped(62)
                                        .next(AsgQuery.Builder.ePropGroup(63,EProp.of(64, NAME.type, Constraint.of(ConstraintOp.notContains, "bob"))))
                                ))
                        , rel(16, FIRE.getrType(), R)
                                .next(concrete(20, "smoge", DRAGON.type, "Display:smoge", "D")
                                        .next(AsgQuery.Builder.ePropGroup(21,EProp.of(22, NAME.type, Constraint.of(ConstraintOp.eq, "smoge"))))
                                )
                )
                .build();
    }


    @Test
    public void testStripOptionalSingleLevel(){
        AsgQuery query = singleOptional();

        AsgQuery expectedMain = AsgQuery.Builder.start("q", "O")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(AsgQuery.Builder.ePropGroup(2,EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                .next(rel(4, OWN.getrType(), R)
                        .below(relProp(5, of(6, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(7, OntologyTestUtils.DRAGON.type))
                .next(quant1(8, all))
                .in(AsgQuery.Builder.ePropGroup(9, EProp.of(10, NAME.type, Constraint.of(eq, "smith")), EProp.of(11, GENDER.type, Constraint.of(gt, new Value(MALE.ordinal(),MALE.name()))))
                        , rel(16, FIRE.getrType(), R)
                                .next(concrete(20, "smoge", DRAGON.type, "Display:smoge", "D")
                                        .next(AsgQuery.Builder.ePropGroup(21,EProp.of(22, NAME.type, Constraint.of(ConstraintOp.eq, "smoge"))))
                                )
                )
                .build();

        AsgQuery expectedOptionalQuery = AsgQuery.Builder.start("q", "O")
                .next(typed(7, DRAGON.type))
                .next(rel(12, FREEZE.getrType(), R))
                .next(unTyped(13))
                .next(AsgQuery.Builder.ePropGroup(14, EProp.of(15, NAME.type, Constraint.of(ConstraintOp.notContains, "bob")))).build();
        AsgQueryUtil.OptionalStrippedQuery optionalStrippedQuery = AsgQueryUtil.stripOptionals(query);
        Assert.assertNotNull(optionalStrippedQuery.getMainQuery());
        Assert.assertEquals(1, optionalStrippedQuery.getOptionalQueries().size());


        AsgQueryAssert.assertEquals(expectedMain, optionalStrippedQuery.getMainQuery());
        AsgQueryAssert.assertEquals(expectedOptionalQuery, optionalStrippedQuery.getOptionalQueries().get(0)._2);
    }

    @Test
    public void testStripOptionalTwoLevels(){
        AsgQuery query = singleHierarchicalOptional();
        AsgQuery expectedMain =  AsgQuery.Builder.start("q", "O")
                .next(typed(1, "entity1", "A"))
                .next(rel(2, "rel1", R,"R").below(relProp(2, RelProp.of(2, "2", Constraint.of(eq, "value2")))))
                .next(typed(3, "entity2", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(5, EProp.of(5, "prop1", Constraint.of(eq, "value1")), EProp.of(5, "prop2", Constraint.of(gt, "value3"))),
                        rel(6, "rel2", R,"R1")
                                .next(typed(7, "entity3", "C")))
                .build();

        AsgQuery expectedOptionalQuery =  AsgQuery.Builder.start("q", "O")
                .next(typed(3, "entity2", "B"))
                .next(rel(12, "rel4", R).next(typed(13, "entity4", "E")
                                .next(optional(14).next(rel(15, "rel4", R).next(typed(16, "entity4", "F"))))))
                .build();
        AsgQueryUtil.OptionalStrippedQuery optionalStrippedQuery = AsgQueryUtil.stripOptionals(query);
        Assert.assertNotNull(optionalStrippedQuery.getMainQuery());
        Assert.assertEquals(1, optionalStrippedQuery.getOptionalQueries().size());


        AsgQueryAssert.assertEquals(expectedMain, optionalStrippedQuery.getMainQuery());
        AsgQueryAssert.assertEquals(expectedOptionalQuery, optionalStrippedQuery.getOptionalQueries().get(0)._2);
    }

    @Test
    public void testGroupQueryWithSingleTagsByTags(){
        AsgQuery query = singleHierarchicalOptional();
        Map<String, List<AsgEBase<EBase>>> map = AsgQueryUtil.groupByTags(query.getStart());
        Assert.assertNotNull(map);
        Assert.assertTrue(map.containsKey("A"));
        Assert.assertEquals(map.get("A").size(),1);
        Assert.assertTrue(map.containsKey("B"));
        Assert.assertEquals(map.get("B").size(),1);
        Assert.assertTrue(map.containsKey("C"));
        Assert.assertEquals(map.get("C").size(),1);
        Assert.assertTrue(map.containsKey("E"));
        Assert.assertEquals(map.get("E").size(),1);
        Assert.assertTrue(map.containsKey("F"));
        Assert.assertEquals(map.get("F").size(),1);
        Assert.assertTrue(map.containsKey("R"));
        Assert.assertEquals(map.get("R").size(),1);
        Assert.assertTrue(map.containsKey("R1"));
        Assert.assertEquals(map.get("R1").size(),1);
        Assert.assertTrue(map.containsKey("R2"));
        Assert.assertEquals(map.get("R2").size(),1);
    }

    @Test
    public void testGroupQueryWithDuplicateTagsByTags(){
        AsgQuery query = AsgQuery.Builder.start("q", "O")
                .next(typed(1, "entity1", "A"))
                .next(rel(2, "rel1", R,"R").below(relProp(2, RelProp.of(2, "2", Constraint.of(eq, "value2")))))
                .next(typed(3, "entity2", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(5, EProp.of(5, "prop1", Constraint.of(eq, "value1")), EProp.of(5, "prop2", Constraint.of(gt, "value3"))),
                        rel(6, "rel2", R,"R").next(typed(7, "entity3", "C")),
                        optional(11)
                                .next(rel(12, "rel4", R).next(typed(13, "entity4", "C")
                                .next(optional(14).next(rel(15, "rel4", R,"R").next(typed(16, "entity4", "B")))))))
                .build();
        ;
        Map<String, List<AsgEBase<EBase>>> map = AsgQueryUtil.groupByTags(query.getStart());
        Assert.assertNotNull(map);
        Assert.assertTrue(map.containsKey("A"));
        Assert.assertEquals(map.get("A").size(),1);
        Assert.assertTrue(map.containsKey("B"));
        Assert.assertEquals(map.get("B").size(),2);
        Assert.assertTrue(map.containsKey("C"));
        Assert.assertEquals(map.get("C").size(),2);
        Assert.assertFalse(map.containsKey("E"));
        Assert.assertFalse(map.containsKey("F"));
        Assert.assertTrue(map.containsKey("R"));
        Assert.assertEquals(map.get("R").size(),3);
        Assert.assertFalse(map.containsKey("R1"));
        Assert.assertFalse(map.containsKey("R2"));
    }

    @Test
    public void testStripTwoOptionalsSingleLevel(){
        AsgQuery query = twoOptionals();

        AsgQuery expectedMain = AsgQuery.Builder.start("q", "O")
                .next(typed(1, OntologyTestUtils.PERSON.type))
                .next(AsgQuery.Builder.ePropGroup(2,EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189))))
                .next(rel(4, OWN.getrType(), R)
                        .below(relProp(5, of(6, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(7, OntologyTestUtils.DRAGON.type))
                .next(quant1(8, all))
                .in(AsgQuery.Builder.ePropGroup(9, EProp.of(10, NAME.type, Constraint.of(eq, "smith")), EProp.of(11, GENDER.type, Constraint.of(gt, new Value(MALE.ordinal(),MALE.name()))))
                        , rel(16, FIRE.getrType(), R)
                                .next(concrete(20, "smoge", DRAGON.type, "Display:smoge", "D")
                                        .next(AsgQuery.Builder.ePropGroup(21,EProp.of(22, NAME.type, Constraint.of(ConstraintOp.eq, "smoge"))))
                                )
                )
                .build();

        AsgQuery expectedOptionalQuery1 = AsgQuery.Builder.start("q", "O")
                .next(typed(7, DRAGON.type))
                .next(rel(12, FREEZE.getrType(), R))
                .next(unTyped(13))
                .next(AsgQuery.Builder.ePropGroup(14, EProp.of(15, NAME.type, Constraint.of(ConstraintOp.notContains, "bob")))).build();
        AsgQuery expectedOptionalQuery2 = AsgQuery.Builder.start("q", "O")
                .next(typed(7, DRAGON.type))
                .next(rel(61, FREEZE.getrType(), R)
                        .next(unTyped(62)
                                .next(AsgQuery.Builder.ePropGroup(63,EProp.of(64, NAME.type, Constraint.of(ConstraintOp.notContains, "bob"))))
                        )).build();


        AsgQueryUtil.OptionalStrippedQuery optionalStrippedQuery = AsgQueryUtil.stripOptionals(query);
        Assert.assertNotNull(optionalStrippedQuery.getMainQuery());
        Assert.assertEquals(2, optionalStrippedQuery.getOptionalQueries().size());


        AsgQueryAssert.assertEquals(expectedMain, optionalStrippedQuery.getMainQuery());
        AsgQueryAssert.assertEquals(expectedOptionalQuery1, optionalStrippedQuery.getOptionalQueries().get(0)._2);
        AsgQueryAssert.assertEquals(expectedOptionalQuery2, optionalStrippedQuery.getOptionalQueries().get(1)._2);
    }
}
