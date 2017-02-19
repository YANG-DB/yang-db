package com.kayhut.fuse.neo4j.service;

import org.jooby.Jooby;

/**
 * Created by EladW on 19/02/2017.
 */
public class FuseNeo4jApp extends Jooby {

    {
        get("/fuse/neo/stat", () -> "Fuse Neo4j server is running...");
    }

    public static void main(final String[] args) {
        run(FuseNeo4jApp::new, args); // start the application.
    }
}


