package com.yangdb.dragons.cursor;

import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.executor.cursor.discrete.NewGraphHierarchyTraversalCursor;
import com.yangdb.fuse.model.results.QueryResultBase;
import com.yangdb.fuse.model.transport.cursor.CreateGraphCursorRequest.GraphFormat;
import com.yangdb.fuse.model.transport.cursor.LogicalGraphCursorRequest;

public class LogicalGraphHierarchyTraversalCursor implements Cursor<TraversalCursorContext> {

    private Cursor<TraversalCursorContext> innerCursor;
    private GraphFormat format;

    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new LogicalGraphHierarchyTraversalCursor(
                    (TraversalCursorContext) context,
                    ((LogicalGraphCursorRequest) context.getCursorRequest()).getCountTags(),
                    ((LogicalGraphCursorRequest) context.getCursorRequest()).getFormat());
        }
        //endregion
    }

    public LogicalGraphHierarchyTraversalCursor(TraversalCursorContext context, Iterable<String> countTags, GraphFormat format) {
        this.format = format;
        innerCursor = new NewGraphHierarchyTraversalCursor(context,countTags);
    }

    @Override
    public QueryResultBase getNextResults(int numResults) {
        return innerCursor.getNextResults(numResults);
    }

    @Override
    public TraversalCursorContext getContext() {
        return innerCursor.getContext();
    }
}
