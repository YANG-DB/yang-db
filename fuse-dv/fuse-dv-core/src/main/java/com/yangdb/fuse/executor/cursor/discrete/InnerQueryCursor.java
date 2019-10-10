package com.yangdb.fuse.executor.cursor.discrete;

/*-
 *
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 yangdb
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.executor.CompositeTraversalCursorContext;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.model.results.*;
import com.yangdb.fuse.model.transport.cursor.CreateInnerQueryCursorRequest;

import java.util.*;

public class InnerQueryCursor implements Cursor {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new InnerQueryCursor(
                    (CompositeTraversalCursorContext) context,
                    new PathsTraversalCursor((TraversalCursorContext) context),
                    (CreateInnerQueryCursorRequest) context.getCursorRequest());
        }
        //endregion
    }
    //endregion

    //region Constructors
    public InnerQueryCursor(CompositeTraversalCursorContext context, PathsTraversalCursor innerCursor, CreateInnerQueryCursorRequest cursorRequest) {
        this.context = context;
        this.innerCursor = innerCursor;
        this.cursorRequest = cursorRequest;
    }
    //endregion

    //region Cursor Implementation

    /**
     * drain all possible results
     *
     * @param numResults
     * @return
     */
    @Override
    public QueryResultBase getNextResults(int numResults) {
        AssignmentsQueryResult all = new AssignmentsQueryResult(new ArrayList<>());
        AssignmentsQueryResult nextResult;
        do {
            //todo - implement the following logic
            // 1) for each inner query - drain results (first page ?) -> this step was already done in PageDriver -> createInnerPage()
            // 2) for each assignment in the resulting inner query:
            //      2.1)  - run search plan & generate path traversal for outer query with parameter
            //      2.1.1)  - execute the outer query with the given assignment as parameter to the query
            //      2.2)  - drain all the results and tag the assignments in that collection as related to the parameter

            nextResult = this.innerCursor.getNextResults(numResults);
            all.getAssignments().addAll(nextResult.getAssignments());
        }
        while (nextResult.getSize() > 0);
        return all;
    }

    @Override
    public TraversalCursorContext getContext() {
        return innerCursor.getContext();
    }


    public CreateInnerQueryCursorRequest getCursorRequest() {
        return cursorRequest;
    }

//endregion

    //region Fields
    private CompositeTraversalCursorContext context;
    private PathsTraversalCursor innerCursor;
    private CreateInnerQueryCursorRequest cursorRequest;
    //endregion
}
