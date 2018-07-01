package com.kayhut.fuse.executor.cursor.promise;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.executor.cursor.promise.TraversalCursor;

/**
 * Created by Roman on 05/04/2017.
 */
public class TraversalCursorFactory implements CursorFactory {
    //region CursorFactory Implementation
    @Override
    public Cursor createCursor(Context context) {
        TraversalCursorContext traversalCursorContext = (TraversalCursorContext)context;
        return new TraversalCursor(traversalCursorContext);
    }
    //endregion
}
