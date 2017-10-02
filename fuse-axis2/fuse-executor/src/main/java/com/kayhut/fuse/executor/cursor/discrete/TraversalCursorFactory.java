package com.kayhut.fuse.executor.cursor.discrete;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;

/**
 * Created by Roman on 05/04/2017.
 */
public class TraversalCursorFactory implements CursorFactory {
    //region CursorFactory Implementation
    @Override
    public Cursor createCursor(Context context) {
        TraversalCursorContext traversalCursorContext = (TraversalCursorContext)context;
        switch (traversalCursorContext.getCursorType()) {
            case paths: return new PathsTraversalCursor(traversalCursorContext);
            case graph: return new GraphTraversalCursor(new PathsTraversalCursor(traversalCursorContext));
            default: return new PathsTraversalCursor(traversalCursorContext);
        }

    }
    //endregion
}
