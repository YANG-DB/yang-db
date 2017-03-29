package com.kayhut.fuse.neo4j.test.cypher;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by EladW on 19/02/2017.
 */
public class KnownQueriesCompilerTest {

    @Test
    @Ignore
    public void shouldCompileV1Q3() {

        try {

            String ontology = new Scanner(new File("C:\\Elad\\Cypher\\dragon_ont.json")).useDelimiter("\\Z").next();


            //TODO: Update Tests!

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
