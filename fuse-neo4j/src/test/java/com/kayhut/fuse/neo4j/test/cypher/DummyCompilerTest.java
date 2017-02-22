package com.kayhut.fuse.neo4j.test.cypher;

import com.kayhut.fuse.neo4j.cypher.Compiler;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by EladW on 19/02/2017.
 */
public class DummyCompilerTest {

    @Test
    public void checkDummyCompiler() {

        String v1Query = "{\n" +
                         "  \"ont\": \"Dragons\",\n" +
                         "  \"name\": \"Q1\",\n" +
                         "  \"elements\": []}";

        Compiler cmp = new Compiler();
        String cypherQuery = cmp.compile(v1Query);
        assertTrue(cypherQuery != null);
    }

}
