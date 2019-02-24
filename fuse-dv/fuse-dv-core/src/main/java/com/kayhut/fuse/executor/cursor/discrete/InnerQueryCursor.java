package com.kayhut.fuse.executor.cursor.discrete;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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
 * #L%
 */

import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import com.kayhut.fuse.model.results.*;
import com.kayhut.fuse.model.transport.cursor.CreateHierarchyFlattenCursorRequest;
import com.kayhut.fuse.model.transport.cursor.CreateInnerQueryCursorRequest;
import com.opencsv.CSVWriter;
import javaslang.collection.Stream;
import javaslang.control.Option;

import java.io.StringWriter;
import java.util.*;

public class InnerQueryCursor implements Cursor {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new InnerQueryCursor(
                    new PathsTraversalCursor((TraversalCursorContext)context),
                    (CreateInnerQueryCursorRequest)context.getCursorRequest());
        }
        //endregion
    }
    //endregion

    //region Constructors
    public InnerQueryCursor(PathsTraversalCursor innerCursor, CreateInnerQueryCursorRequest cursorRequest) {
        this.innerCursor = innerCursor;
        this.cursorRequest = cursorRequest;
    }
    //endregion

    //region Cursor Implementation

    /**
     * drain all possible results
     * @param numResults
     * @return
     */
    @Override
    public QueryResultBase getNextResults(int numResults) {
        AssignmentsQueryResult all = new AssignmentsQueryResult(new ArrayList<>());
        AssignmentsQueryResult nextResult;
        do{
            nextResult = this.innerCursor.getNextResults(numResults);
            all.getAssignments().addAll(nextResult.getAssignments());
        }
        while(nextResult.getSize() > 0);
        return all;
    }

    //endregion

    //region Fields
    private PathsTraversalCursor innerCursor;
    private CreateInnerQueryCursorRequest cursorRequest;
    //endregion
}
