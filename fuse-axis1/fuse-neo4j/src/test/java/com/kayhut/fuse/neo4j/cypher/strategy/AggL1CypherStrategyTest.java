package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.aggregation.AggL1;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.rel;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.unTyped;

/**
 * Created by Elad on 6/12/2017.
 */
public class AggL1CypherStrategyTest {

    @Test
    public void testAggL1Strategy() {

        AggL1 agg = new AggL1();
        agg.setCon(Constraint.of(ConstraintOp.lt, 20));
        agg.setPer(new String[]{"r1"});
        agg.seteNum(3);

        AsgQuery query = AsgQuery.Builder.start("aggL1", "dragons")
                .next(unTyped(1,"A"))
                .next(rel(2,1, Rel.Direction.R).below(AsgEBase.Builder.get().withEBase(agg).build()))
                .next(unTyped(4,"B"))
                .build();

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withOnt("dragons")
                .withEntityTypes(
                        Collections.singletonList(new EntityType(1, "person", Collections.emptyList()))
                ).withRelationshipTypes(
                        Collections.singletonList(new RelationshipType("knows",1,true))
                ).build();


        String cypher = CypherCompiler.compile(query ,ontology);

        //TODO: verify query correctness -
        //TODO:     if aggregation is done on r1, it should not be returned (?)
        Assert.assertEquals(cypher, "MATCH\n" +
                "p0 = (A)-[r1:knows]->(B)\n" +
                "WITH count(r1) AS agg1,A,r1,B\n" +
                "WHERE agg1 < 20\n" +
                "RETURN A,r1,agg1,B\n");

    }

}