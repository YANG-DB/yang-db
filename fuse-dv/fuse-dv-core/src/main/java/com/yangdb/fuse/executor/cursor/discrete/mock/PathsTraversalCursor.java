package com.yangdb.fuse.executor.cursor.discrete.mock;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.cursor.CursorFactory;
import com.yangdb.fuse.executor.cursor.TraversalCursorContext;
import com.yangdb.fuse.model.results.AssignmentsQueryResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by roman.margolis on 08/11/2017.
 */
public class PathsTraversalCursor implements Cursor<TraversalCursorContext> {
    //region Factory
    public static class Factory implements CursorFactory {
        //region CursorFactory Implementation
        @Override
        public Cursor createCursor(Context context) {
            return new PathsTraversalCursor(
                    (TraversalCursorContext)context,
                    Paths.get(System.getProperty("user.dir"), "mockResults").toString());
        }
        //endregion
    }
    //endregion

    //region Constructors
    public PathsTraversalCursor(TraversalCursorContext context, String mockResultsFolder) {
        this.context = context;
        this.mockResultsFolder = mockResultsFolder;
        this.mapper = new ObjectMapper();
    }
    //endregion

    //region Cursor Implementation
    @Override
    public AssignmentsQueryResult getNextResults(int numResults) {
        String queryName = context.getQueryResource().getQuery().getName();
        try {
            return this.mapper.readValue(new File(Paths.get(this.mockResultsFolder, queryName + ".json").toString()), AssignmentsQueryResult.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new AssignmentsQueryResult();
        }
    }

    @Override
    public TraversalCursorContext getContext() {
        return context;
    }
    //endregion

    //region Fields
    private TraversalCursorContext context;
    private String mockResultsFolder;
    private ObjectMapper mapper;
    //endregion
}
