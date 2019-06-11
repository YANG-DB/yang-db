package com.kayhut.fuse.dispatcher.cursor;

/*-
 * #%L
 * fuse-core
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

import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;

/**
 * Created by Roman on 02/04/2017.
 */
public interface CursorFactory {
    interface Context {
        QueryResource getQueryResource();
        CreateCursorRequest getCursorRequest();

        class Impl implements Context{
            //region Constructors
            public Impl(QueryResource queryResource, CreateCursorRequest cursorRequest) {
                this.queryResource = queryResource;
                this.cursorRequest = cursorRequest;
            }
            //endregion

            //region Context Implementation
            public QueryResource getQueryResource() {
                return queryResource;
            }

            public CreateCursorRequest getCursorRequest() {
                return cursorRequest;
            }
            //endregion

            //region Fields
            private QueryResource queryResource;
            private CreateCursorRequest cursorRequest;
            //endregion
        }
    }

    Cursor createCursor(Context context);
}
