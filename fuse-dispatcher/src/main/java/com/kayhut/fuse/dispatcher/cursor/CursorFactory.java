package com.kayhut.fuse.dispatcher.cursor;

import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.model.transport.CreateCursorRequest;

/**
 * Created by Roman on 02/04/2017.
 */
public interface CursorFactory {
    interface Context {
        QueryResource getQueryResource();
        CreateCursorRequest.CursorType getCursorType();

        class Impl implements Context{
            //region Constructors
            public Impl(QueryResource queryResource, CreateCursorRequest.CursorType cursorType) {
                this.queryResource = queryResource;
                this.cursorType = cursorType;
            }
            //endregion

            //region Context Implementation
            public QueryResource getQueryResource() {
                return queryResource;
            }

            public CreateCursorRequest.CursorType getCursorType() {
                return cursorType;
            }
            //endregion

            //region Fields
            private QueryResource queryResource;
            private CreateCursorRequest.CursorType cursorType;
            //endregion
        }
    }

    Cursor createCursor(Context context);
}
