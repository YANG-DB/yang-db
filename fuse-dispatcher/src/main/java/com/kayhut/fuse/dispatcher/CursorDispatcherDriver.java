package com.kayhut.fuse.dispatcher;

import com.kayhut.fuse.model.process.command.CursorCommand;

/**
 * Created by lior on 21/02/2017.
 */
public interface CursorDispatcherDriver {
    CursorCommand fetch(String cursorId, long fetchSize);

    CursorCommand plan(String cursorId);

    CursorCommand delete(String cursorId);
}
