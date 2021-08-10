package com.yangdb.fuse.dispatcher.profile;

/**
 * add provisioning capability for query cursor runtime
 */
public interface CursorRuntimeProvision {
    int getActiveScrolls();
    int clearScrolls();

    static class NoOpCursorRuntimeProvision implements CursorRuntimeProvision{

        public static final CursorRuntimeProvision INSTANCE = new NoOpCursorRuntimeProvision();

        @Override
        public int getActiveScrolls() {
            return 0;
        }

        @Override
        public int clearScrolls() {
            return 0;
        }
    }
}
