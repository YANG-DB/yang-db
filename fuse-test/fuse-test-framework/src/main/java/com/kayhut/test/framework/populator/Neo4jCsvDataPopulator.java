package com.kayhut.test.framework.populator;

import com.kayhut.test.framework.scenario.FileCsvDataProvider;
import com.kayhut.test.framework.scenario.GenericDataProvider;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;

import java.io.IOException;

/**
 * Created by moti on 3/20/2017.
 */
public class Neo4jCsvDataPopulator implements DataPopulator{

    private GraphDatabaseService graphDatabaseService;
    private FileCsvDataProvider dataProvider;

    public Neo4jCsvDataPopulator(GraphDatabaseService graphDatabaseService, FileCsvDataProvider dataProvider) {
        this.graphDatabaseService = graphDatabaseService;
        this.dataProvider = dataProvider;
    }

    @Override
    public void populate() throws IOException {
        Result executionResult = this.graphDatabaseService.execute(this.dataProvider.getCsvCypher());
        if (executionResult.hasNext())
            throw new RuntimeException("Insertion failed");

    }
}
