package com.kayhut.fuse.neo4j.test.cypher;

import com.kayhut.fuse.neo4j.cypher.Compiler;
import com.kayhut.fuse.neo4j.cypher.Schema;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

/**
 * Created by EladW on 19/02/2017.
 */
public class KnownQueriesCompilerTest {

    @Test
    public void shouldCompileV1Q3() {

        try {

            String ontology = new Scanner(new File("C:\\Elad\\Cypher\\dragon_ont.json")).useDelimiter("\\Z").next();

            Schema schema = new Schema();

            schema.load(ontology);

            String v1Query = new Scanner(new File("C:\\Elad\\Cypher\\q3.json")).useDelimiter("\\Z").next();

            Compiler cmp = new Compiler();

            String cypherQuery = cmp.compile(v1Query, schema);

            assertTrue(cypherQuery != null &&
                                    cypherQuery.equals("MATCH (A:Dragon)<-[:own]-(B:Person) WHERE B.first_name = 'Allison' RETURN A ,B "));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
