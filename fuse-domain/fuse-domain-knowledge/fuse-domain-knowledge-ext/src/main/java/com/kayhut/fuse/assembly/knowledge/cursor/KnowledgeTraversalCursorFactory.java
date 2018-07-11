package com.kayhut.fuse.assembly.knowledge.cursor;

import com.kayhut.fuse.assembly.knowledge.logical.cursor.KnowledgeLogicalModelTraversalCursor;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.executor.cursor.discrete.CsvTraversalCursor;
import com.kayhut.fuse.executor.cursor.discrete.GraphTraversalCursor;
import com.kayhut.fuse.executor.cursor.discrete.HierarchyFlattenCursor;
import com.kayhut.fuse.executor.cursor.discrete.PathsTraversalCursor;
import com.kayhut.fuse.model.transport.cursor.*;

/**
 * Created by Roman on 05/04/2017.
 */
public class KnowledgeTraversalCursorFactory implements CursorFactory {
    //region CursorFactory Implementation
    @Override
    public Cursor createCursor(Context context) {
        TraversalCursorContext traversalCursorContext = (TraversalCursorContext)context;

        if (traversalCursorContext.getCursorRequest() instanceof CreatePathsCursorRequest) {
            return new PathsTraversalCursor(traversalCursorContext);
        } else if (traversalCursorContext.getCursorRequest() instanceof CreateGraphCursorRequest) {
            return new GraphTraversalCursor(new PathsTraversalCursor(traversalCursorContext));
        } else if (traversalCursorContext.getCursorRequest() instanceof CreateGraphHierarchyCursorRequest) {
              return new KnowledgeGraphHierarchyTraversalCursor(traversalCursorContext,
                    ((CreateGraphHierarchyCursorRequest)traversalCursorContext.getCursorRequest()).getCountTags());
        } else if (traversalCursorContext.getCursorRequest() instanceof CreateLogicalGraphHierarchyCursorRequest) {
              return new KnowledgeLogicalModelTraversalCursor(traversalCursorContext,
                    ((CreateLogicalGraphHierarchyCursorRequest)traversalCursorContext.getCursorRequest()).getCountTags());
        }else if(traversalCursorContext.getCursorRequest() instanceof CreateCsvCursorRequest){
            return new CsvTraversalCursor(new PathsTraversalCursor(traversalCursorContext), (CreateCsvCursorRequest) traversalCursorContext.getCursorRequest());
        }
        else if(traversalCursorContext.getCursorRequest() instanceof CreateHierarchyFlattenCursorRequest){
            return new HierarchyFlattenCursor(new PathsTraversalCursor(traversalCursorContext), (CreateHierarchyFlattenCursorRequest)traversalCursorContext.getCursorRequest());
        }
        else {
            return new PathsTraversalCursor(traversalCursorContext);
        }
    }
    //endregion
}
