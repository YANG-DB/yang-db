package com.kayhut.fuse.executor.cursor.discrete.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by roman.margolis on 08/11/2017.
 */
public class PathsTraversalCursor implements Cursor {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new PathsTraversalCursor(
                    (TraversalCursorContext)context,
                    Paths.get(System.getProperty("user.dir"), "mockResults").toString());
        }
        //endregion
    }
    //endregion

    //region Constructors
    public PathsTraversalCursor(TraversalCursorContext context, String mockResultsFolder) {
        this.context = context;
        this.mockResultsFolder = mockResultsFolder;
        this.mapper = new ObjectMapper();
    }
    //endregion

    //region Cursor Implementation
    @Override
    public AssignmentsQueryResult getNextResults(int numResults) {
        String queryName = context.getQueryResource().getQuery().getName();
        try {
            return this.mapper.readValue(new File(Paths.get(this.mockResultsFolder, queryName + ".json").toString()), AssignmentsQueryResult.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new AssignmentsQueryResult();
        }
    }
    //endregion

    //region Fields
    private TraversalCursorContext context;
    private String mockResultsFolder;
    private ObjectMapper mapper;
    //endregion
}
