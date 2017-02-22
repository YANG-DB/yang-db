package com.kayhut.fuse.neo4j.cypher;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Created by EladW on 19/02/2017.
 */
public class Compiler {

    Logger logger = LoggerFactory.getLogger("com.kayhut.fuse.neo4j.cypher.Compiler");

    public String compile(String query) {

        ObjectMapper mapper = new ObjectMapper();

        // At first stage we will have a collection of v1 queries written in cypher,
        // and we only support querying ine of these queries, identified by query-id.

        try {

            Map map = mapper.readValue(query, Map.class);

            if(map.containsKey("ont") &&
               map.get("ont").equals("Dragons")) {

                if(map.containsKey("name")) {

                    String queryName = (String) map.get("name");

                    if(queryName.equals("Q1")) {
                        String cypher = "match (p:Person)-[:own]->(:dragon) where p.name = 'Brandon Stark' return p";
                        return cypher;
                    }

                }

            }

        } catch (IOException e) {
            logger.error("JSON parsing failed.");
        }

        return null;
    }

}
