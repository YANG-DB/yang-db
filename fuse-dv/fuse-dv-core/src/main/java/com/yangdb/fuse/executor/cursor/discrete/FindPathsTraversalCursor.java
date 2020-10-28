package com.yangdb.fuse.executor.cursor.discrete;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.model.results.Assignment;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;

import static com.yangdb.fuse.model.results.AssignmentsQueryResult.Builder.instance;
import static com.yangdb.fuse.model.results.AssignmentsQueryResult.distinct;


public class FindPathsTraversalCursor extends PathsTraversalCursor {

    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new FindPathsTraversalCursor((TraversalCursorContext) context);
        }
        //endregion
    }
    //endregion

    //region Constructors
    public FindPathsTraversalCursor(TraversalCursorContext context) {
        super(context);
    }
    //endregion

    //region Private Methods
    protected AssignmentsQueryResult toQuery(int numResults) {
        AssignmentsQueryResult.Builder builder = instance();
        //
        do {
            (getContext().getTraversal().next(numResults)).forEach(path -> {
                Assignment assignments = toAssignment(path);
                builder.withAssignment(assignments);
            });
        } while (distinct(builder.build()).getSize() < numResults && getContext().getTraversal().hasNext());
        return distinct(builder.build());
    }

}
