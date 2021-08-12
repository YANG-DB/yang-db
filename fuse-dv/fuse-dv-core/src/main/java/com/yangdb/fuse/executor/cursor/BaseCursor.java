package com.yangdb.fuse.executor.cursor;

import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.provision.CursorRuntimeProvision;

public abstract class BaseCursor implements Cursor<TraversalCursorContext> {
    protected TraversalCursorContext context;
    protected CursorRuntimeProvision runtimeProvision;

    public BaseCursor(TraversalCursorContext context) {
        this.context = context;
        this.runtimeProvision = context.getRuntimeProvision();
    }

    @Override
    public int getActiveScrolls() {
        return runtimeProvision.getActiveScrolls();
    }

    @Override
    public boolean clearScrolls() {
        return runtimeProvision.clearScrolls();
    }

    public TraversalCursorContext getContext() {
        return context;
    }

}
