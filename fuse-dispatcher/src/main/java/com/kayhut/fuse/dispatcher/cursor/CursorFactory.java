package com.kayhut.fuse.dispatcher.cursor;

import com.kayhut.fuse.dispatcher.resource.QueryResource;

/**
 * Created by Roman on 02/04/2017.
 */
public interface CursorFactory {
    interface Context {
        QueryResource getQueryResource();
    }

    Cursor createCursor(Context context);
}
