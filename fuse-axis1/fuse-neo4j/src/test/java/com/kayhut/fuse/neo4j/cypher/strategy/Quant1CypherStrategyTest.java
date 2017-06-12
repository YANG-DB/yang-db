package com.kayhut.fuse.neo4j.cypher.strategy;

import com.google.common.collect.Lists;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;

/**
 * Created by Elad on 6/11/2017.
 */
public class Quant1CypherStrategyTest {

    //A quantifier has one connection on its L side, and two or more branches on its R side
    //In quantifiers of type 1, the L side always ends with an entity, and the R side starts with:
    //          quantifier, path, relation or entity's property.

    @Test
    public void testQuant1StrategyAllSinglePropertyOnRight() {

        EProp eProp = new EProp(1, "1", Constraint.of(ConstraintOp.eq, 12));
        eProp.setpTag("P");

        AsgQuery query = AsgQuery.Builder.start("quant1_all_prop", "dragons")
                .next(unTyped(1,"A"))
                .next(quant1(2, QuantType.all))
                .next(eProp(3,eProp))
                .build();

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withOnt("dragons")
                .withEntityTypes(
                        Collections.singletonList(new EntityType(1, "person", Collections.emptyList()))
                ).withProperties(
                        Collections.singletonList(Property.Builder.get().build(1,"p1","int"))
                ).build();


        String cypher = CypherCompiler.compile(query ,ontology);

        Assert.assertEquals(cypher, "MATCH\n" +
                "p0 = (A)\n" +
                "WHERE A.p1 = 12\n" +
                "RETURN A\n");

    }

    @Test
    public void testQuant1StrategyAllMultiplePropertiesOnRight() {

        EProp eProp1 = new EProp(1, "1", Constraint.of(ConstraintOp.eq, "str_val"));
        eProp1.setpTag("P1");

        EProp eProp2 = new EProp(1, "2", Constraint.of(ConstraintOp.le, 12));
        eProp2.setpTag("P2");

        EProp eProp3 = new EProp(1, "3", Constraint.of(ConstraintOp.gt, 10));
        eProp3.setpTag("P3");

        AsgQuery query = AsgQuery.Builder.start("quant1_all_props", "dragons")
                .next(unTyped(1,"A"))
                .next(quant1(2, QuantType.all))
                .next(eProp(3,eProp1, eProp2, eProp3))
                .build();

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withOnt("dragons")
                .withEntityTypes(
                        Collections.singletonList(new EntityType(1, "person", Collections.emptyList()))
                ).withProperties(
                        Lists.newArrayList(Property.Builder.get().build(1, "p1", "string"),
                                           Property.Builder.get().build(2, "p2", "int"),
                                           Property.Builder.get().build(3, "p3", "int"))
                ).build();


        String cypher = CypherCompiler.compile(query ,ontology);

        Assert.assertEquals(cypher, "MATCH\n" +
                "p0 = (A)\n" +
                "WHERE A.p1 = 'str_val' AND A.p2 <= 12 AND A.p3 > 10\n" +
                "RETURN A\n");

    }

    @Test
    public void testQuant1StrategySomeMultiplePropertiesOnRight() {

        EProp eProp1 = new EProp(1, "1", Constraint.of(ConstraintOp.eq, "str_val"));
        eProp1.setpTag("P1");

        EProp eProp2 = new EProp(1, "2", Constraint.of(ConstraintOp.le, 12));
        eProp2.setpTag("P2");

        EProp eProp3 = new EProp(1, "3", Constraint.of(ConstraintOp.gt, 10));
        eProp3.setpTag("P3");

        AsgQuery query = AsgQuery.Builder.start("quant1_some_props", "dragons")
                .next(unTyped(1,"A"))
                .next(quant1(2, QuantType.some))
                .next(eProp(3,eProp1, eProp2, eProp3))
                .build();

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withOnt("dragons")
                .withEntityTypes(
                        Collections.singletonList(new EntityType(1, "person", Collections.emptyList()))
                ).withProperties(
                        Lists.newArrayList(Property.Builder.get().build(1, "p1", "string"),
                                Property.Builder.get().build(2, "p2", "int"),
                                Property.Builder.get().build(3, "p3", "int"))
                ).build();


        String cypher = CypherCompiler.compile(query ,ontology);

        Assert.assertEquals(cypher, "MATCH\n" +
                "p0 = (A)\n" +
                "WHERE A.p1 = 'str_val' OR A.p2 <= 12 OR A.p3 > 10\n" +
                "RETURN A\n");

    }

    @Test
    public void testQuant1StrategyAllSingleRelOnRight() {

        AsgQuery query = AsgQuery.Builder.start("quant1_all_rel", "dragons")
                .next(unTyped(1,"A"))
                .next(quant1(2, QuantType.all))
                .next(rel(3, 1, Rel.Direction.R))
                .next(unTyped(1,"B"))
                .build();

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withOnt("dragons")
                .withEntityTypes(
                        Collections.singletonList(new EntityType(1, "person", Collections.emptyList()))
                ).withRelationshipTypes(
                        Collections.singletonList(new RelationshipType("knows", 1, false))
                ).build();


        String cypher = CypherCompiler.compile(query ,ontology);

        Assert.assertEquals(cypher, "MATCH\n" +
                "p0 = (A)-[r1:knows]->(B)\n" +
                "RETURN A,r1,B\n");

    }

    @Test
    public void testQuant1StrategyAllMultipleRelsOnRight() {

        AsgQuery query = AsgQuery.Builder.start("quant1_all_rels", "dragons")
                .next(unTyped(1,"A"))
                .next(quant1(2, QuantType.all))
                .in(rel(3, 1, Rel.Direction.R).next(unTyped(1,"C")),
                        rel(4, 2, Rel.Direction.R).next(unTyped(1,"D")),
                        rel(5, 3, Rel.Direction.R).next(unTyped(1,"E")))
                .build();

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withOnt("dragons")
                .withEntityTypes(
                        Collections.singletonList(new EntityType(1, "person", Collections.emptyList()))
                ).withRelationshipTypes(
                        Lists.newArrayList(new RelationshipType("knows", 1, false),
                                new RelationshipType("loves", 2, false),
                                new RelationshipType("hates", 3, false))
                ).build();


        String cypher = CypherCompiler.compile(query ,ontology);

        Assert.assertEquals(cypher, "MATCH\n" +
                "p0 = (A)-[r1:knows]->(C)\n" +
                ",p1 = (A)-[r2:loves]->(D)\n" +
                ",p2 = (A)-[r3:hates]->(E)\n" +
                "RETURN A,r1,r2,r3,C,D,E\n");

    }

    @Test
    public void testQuant1StrategySomeMultipleRelsOnRight() {

        AsgQuery query = AsgQuery.Builder.start("quant1_some_rels", "dragons")
                .next(unTyped(1,"A"))
                .next(quant1(2, QuantType.some))
                .in(rel(3, 1, Rel.Direction.R).next(unTyped(1,"C")),
                    rel(4, 2, Rel.Direction.R).next(unTyped(1,"D")),
                    rel(5, 3, Rel.Direction.R).next(unTyped(1,"E")))
                .build();

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withOnt("dragons")
                .withEntityTypes(
                        Collections.singletonList(new EntityType(1, "person", Collections.emptyList()))
                ).withRelationshipTypes(
                        Lists.newArrayList(new RelationshipType("knows", 1, false),
                                           new RelationshipType("loves", 2, false),
                                           new RelationshipType("hates", 3, false))
                ).build();


        String cypher = CypherCompiler.compile(query ,ontology);

        //TODO: check if cypher can handle returning more data in one branch than the others (i.e. longer branches)

        Assert.assertTrue(cypher.contains("MATCH\n" +
                "p0 = (A)-[r1:knows]->(C)\n" +
                "RETURN A,r1\n"));

        Assert.assertTrue(cypher.contains("MATCH\n" +
                "p0 = (A)-[r1:hates]->(E)\n" +
                "RETURN A,r1\n"));

        Assert.assertTrue(cypher.contains("MATCH\n" +
                "p0 = (A)-[r1:loves]->(D)\n" +
                "RETURN A,r1\n"));

    }
}