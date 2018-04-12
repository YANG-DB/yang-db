package com.kayhut.fuse.executor.cursor.discrete;

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.model.transport.cursor.CreateCsvCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphHierarchyCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreatePathsCursorRequest;

/**
 * Created by Roman on 05/04/2017.
 */
public class TraversalCursorFactory implements CursorFactory {
    //region CursorFactory Implementation
    @Override
    public Cursor createCursor(Context context) {
        TraversalCursorContext traversalCursorContext = (TraversalCursorContext)context;

        if (traversalCursorContext.getCursorRequest() instanceof CreatePathsCursorRequest) {
            return new PathsTraversalCursor(traversalCursorContext);
        } else if (traversalCursorContext.getCursorRequest() instanceof CreateGraphCursorRequest) {
            return new GraphTraversalCursor(new PathsTraversalCursor(traversalCursorContext));
        } else if (traversalCursorContext.getCursorRequest() instanceof CreateGraphHierarchyCursorRequest) {
            /*return new GraphHierarchyTraversalCursor(
                    new PathsTraversalCursor(traversalCursorContext),
                    ((CreateGraphHierarchyCursorRequest)traversalCursorContext.getCursorRequest()).getCountTags());*/

            return new NewGraphHierarchyTraversalCursor(traversalCursorContext,
                    ((CreateGraphHierarchyCursorRequest)traversalCursorContext.getCursorRequest()).getCountTags());
        }else if(traversalCursorContext.getCursorRequest() instanceof CreateCsvCursorRequest){
            return new CsvTraversalCursor(new PathsTraversalCursor(traversalCursorContext), (CreateCsvCursorRequest) traversalCursorContext.getCursorRequest());
        }
        else {
            return new PathsTraversalCursor(traversalCursorContext);
        }
    }
    //endregion
}
