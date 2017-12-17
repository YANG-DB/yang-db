package com.kayhut.fuse.executor.cursor.discrete.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.results.QueryResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by roman.margolis on 08/11/2017.
 */
public class PathsTraversalCursor implements Cursor {
    //region Constructors
    public PathsTraversalCursor(TraversalCursorContext context, String mockResultsFolder) {
        this.context = context;
        this.mockResultsFolder = mockResultsFolder;
        this.mapper = new ObjectMapper();
    }
    //endregion

    //region Cursor Implementation
    @Override
    public QueryResult getNextResults(int numResults) {
        String queryName = context.getQueryResource().getQuery().getName();
        try {
            return this.mapper.readValue(new File(Paths.get(this.mockResultsFolder, queryName + ".json").toString()), QueryResult.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new QueryResult();
        }
    }
    //endregion

    //region Fields
    private TraversalCursorContext context;
    private String mockResultsFolder;
    private ObjectMapper mapper;
    //endregion
}