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
    }

    Cursor createCursor(Context context);
}
