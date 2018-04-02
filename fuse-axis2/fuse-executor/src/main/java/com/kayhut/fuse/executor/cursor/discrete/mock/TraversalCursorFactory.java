package com.kayhut.fuse.executor.cursor.discrete.mock;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.executor.cursor.discrete.GraphHierarchyTraversalCursor;
import com.kayhut.fuse.executor.cursor.discrete.GraphTraversalCursor;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreatePathsCursorRequest;

import java.nio.file.Paths;

/**
 * Created by roman.margolis on 08/11/2017.
 */
public class TraversalCursorFactory implements CursorFactory {
    //region Constructors
    public TraversalCursorFactory() {
        this.workingDir = System.getProperty("user.dir");
    }
    //endregion

    //region CursorFactory Implementation
    @Override
    public Cursor createCursor(Context context) {
        TraversalCursorContext traversalCursorContext = (TraversalCursorContext)context;

        if (traversalCursorContext.getCursorRequest() instanceof CreatePathsCursorRequest) {
            return new PathsTraversalCursor(traversalCursorContext, Paths.get(this.workingDir, "mockResults").toString());
        } else if (traversalCursorContext.getCursorRequest() instanceof CreateGraphCursorRequest) {
            return new GraphTraversalCursor(new PathsTraversalCursor(traversalCursorContext,
                    Paths.get(this.workingDir, "mockResults").toString()));
        } else if (traversalCursorContext.getCursorRequest() instanceof CreateGraphHierarchyCursorRequest) {
            return null;
        } else {
            return new com.kayhut.fuse.executor.cursor.discrete.PathsTraversalCursor(traversalCursorContext);
        }
    }
    //endregion

    //region Fields
    private String workingDir;
    //endregion
}
