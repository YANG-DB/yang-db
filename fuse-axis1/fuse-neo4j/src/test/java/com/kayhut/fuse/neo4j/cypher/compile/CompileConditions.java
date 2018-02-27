package com.kayhut.fuse.neo4j.cypher.compile;

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
import com.kayhut.fuse.neo4j.cypher.TestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by Elad on 7/2/2017.
 */
public class CompileConditions {

    private Ontology ontology;

    @Before
    public void setup() throws IOException {
        ontology = TestUtils.loadOntology("Dragons.json");
    }

    @Test
    @Ignore
    public void testCompileOrsAnds() {

        AsgQuery query = AsgQuery.Builder.start("q1", "Dragons")
                .next(AsgQuery.Builder.typed(1, "Person", "A"))
                .next(AsgQuery.Builder.quant1(2, QuantType.all)
                        .next(AsgQuery.Builder.eProp(3, new EProp(3, "firstName", Constraint.of(ConstraintOp.eq, "John")))))
                .next(AsgQuery.Builder.quant1(4, QuantType.some))
                .next(AsgQuery.Builder.eProp(5, new EProp(2, "height", Constraint.of(ConstraintOp.lt, 180)),
                                                       new EProp(2, "firstName", Constraint.of(ConstraintOp.ne, "Doe")))).build();

        String cypher = CypherCompiler.compile(query, ontology);

        Assert.assertEquals("MATCH\n" +
                "p0 = (A:Person)\n" +
                "WHERE A.firstName = 'John' OR A.height < 180 OR A.firstName  <>  'Doe'\n" +
                "RETURN A\n", cypher);
    }
}
