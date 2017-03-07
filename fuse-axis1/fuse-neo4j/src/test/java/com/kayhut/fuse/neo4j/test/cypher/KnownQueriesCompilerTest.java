package com.kayhut.fuse.neo4j.test.cypher;

import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.neo4j.cypher.CypherCompiler;
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

            //TODO: Update Tests!

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
