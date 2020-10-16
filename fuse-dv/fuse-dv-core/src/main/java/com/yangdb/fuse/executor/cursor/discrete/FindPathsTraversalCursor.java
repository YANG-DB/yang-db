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
import com.yangdb.fuse.model.transport.cursor.FindPathTraversalCursorRequest;

import static com.yangdb.fuse.model.results.AssignmentsQueryResult.Builder.instance;


public class FindPathsTraversalCursor extends PathsTraversalCursor {

    private final int amount;

    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new FindPathsTraversalCursor((TraversalCursorContext) context,
                    ((FindPathTraversalCursorRequest) context.getCursorRequest()).getAmount());
        }
        //endregion
    }
    //endregion

    //region Constructors
    public FindPathsTraversalCursor(TraversalCursorContext context, int amount) {
        super(context);
        this.amount = amount;
    }
    //endregion

    //region Private Methods
    protected AssignmentsQueryResult toQuery(int numResults) {
        AssignmentsQueryResult.Builder builder = instance();
        //
        (getContext().getTraversal().next(amount)).forEach(path -> {
            Assignment assignments = toAssignment(path);
            builder.withAssignment(assignments);
        });
        return AssignmentsQueryResult.distinct(builder.build());
    }

}
