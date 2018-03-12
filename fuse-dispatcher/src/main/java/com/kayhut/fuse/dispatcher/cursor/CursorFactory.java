package com.kayhut.fuse.dispatcher.cursor;

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
