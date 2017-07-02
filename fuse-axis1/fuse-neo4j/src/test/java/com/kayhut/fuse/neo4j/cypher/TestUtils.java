package com.kayhut.fuse.neo4j.cypher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by User on 26/03/2017.
 */
public abstract class TestUtils {

    public static Ontology loadOntology(String name) throws IOException {
        String query = IOUtils.toString(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(name)));
        return new ObjectMapper().readValue(query, Ontology.class);
    }

    public static Query loadQuery(String name) throws IOException {
        String query = IOUtils.toString(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(name)));
        return new ObjectMapper().readValue(query, Query.class);
    }

}
