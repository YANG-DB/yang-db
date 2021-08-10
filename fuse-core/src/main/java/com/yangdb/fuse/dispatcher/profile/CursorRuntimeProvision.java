package com.yangdb.fuse.dispatcher.profile;

/**
 * add provisioning capability for query cursor runtime
 */
public interface CursorRuntimeProvision {
    int getActiveScrolls();
    int clearScrolls();
}
