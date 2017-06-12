package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;

/**
 * Created by Elad on 6/11/2017.
 */
public class Quant2CypherStrategyTest {

    //A quantifier has one connection on its L side, and two or more branches on its R side
    //In quantifiers of type 2, the L side always ends with a relation or a path, and the R side starts with:
    //          quantifier or an entity.

    @Test
    public void testQuant2StrategyAllSingleRelOnLeftSingleEntityOnRight() {

        AsgQuery query = AsgQuery.Builder.start("quant2_all_rel_ent", "dragons")
                .next(unTyped(1,"A"))
                .next(rel(2,1, Rel.Direction.R))
                .next(quant2(3, QuantType.all))
                .next(unTyped(4,"B"))
                .build();

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withOnt("dragons")
                .withRelationshipTypes(
                        Collections.singletonList(new RelationshipType("knows",1, true))
                ).build();

        String cypher = CypherCompiler.compile(query ,ontology);

        Assert.assertEquals(cypher, "MATCH\n" +
                "p0 = (A)-[r1:knows]->(B)\n" +
                "RETURN A,r1,B\n");

    }

    @Test
    public void testQuant2StrategySomeSingleRelOnLeftMultipleEntitiesOnRight() {

        AsgQuery query = AsgQuery.Builder.start("quant2_some_rel_ents", "dragons")
                .next(unTyped(1,"A"))
                .next(rel(2,1, Rel.Direction.R))
                .next(quant2(3, QuantType.some))
                .in(unTyped(4,"B"),unTyped(5,"C"),unTyped(6,"D"))
                .build();

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withOnt("dragons")
                .withRelationshipTypes(
                        Collections.singletonList(new RelationshipType("knows",1, true))
                ).build();

        String cypher = CypherCompiler.compile(query ,ontology);

        Assert.assertTrue(cypher.contains("MATCH\n" +
                "p0 = (A)-[r1:knows]->(B)\n" +
                "RETURN A,r1\n"));

        Assert.assertTrue(cypher.contains("MATCH\n" +
                "p0 = (A)-[r1:knows]->(C)\n" +
                "RETURN A,r1\n"));

        Assert.assertTrue(cypher.contains("MATCH\n" +
                "p0 = (A)-[r1:knows]->(D)\n" +
                "RETURN A,r1\n"));


    }

}