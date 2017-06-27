package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.eProp;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.unTyped;

/**
 * Created by Elad on 6/8/2017.
 */
public class ConditionCypherStrategyTest {


    @Test
    public void testConditionStrategy() {

        EProp eProp = new EProp(1, "1", Constraint.of(ConstraintOp.eq, 12));
        eProp.setpTag("P");

        AsgQuery query = AsgQuery.Builder.start("condition", "dragons")
                .next(unTyped(1,"A"))
                .next(eProp(2,eProp))
                .build();

        Ontology ontology = Ontology.OntologyBuilder.anOntology()
                .withOnt("dragons")
                .withEntityTypes(
                        Collections.singletonList(new EntityType("1", "person", Collections.emptyList()))
                ).withProperties(
                        Collections.singletonList(Property.Builder.get().build("1","p1","int"))
                ).build();


        String cypher = CypherCompiler.compile(query ,ontology);

        Assert.assertEquals(cypher, "MATCH\n" +
                "p0 = (A)\n" +
                "WHERE A.p1 = 12\n" +
                "RETURN A\n");

    }


}