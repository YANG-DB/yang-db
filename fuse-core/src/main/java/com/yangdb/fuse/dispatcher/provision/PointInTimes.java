package com.yangdb.fuse.dispatcher.provision;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2026 The YangDb Graph Database Project
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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The OpenSearch Point in Time of each cursor that reads a consistent snapshot (ROADMAP 7.8).
 * <p>
 * Keyed by the cursor's traversal context, {@code <queryId>.<cursorId>}: the context every search of the cursor carries
 * (see {@link ScrollProvisioning.MetricRegistryScrollProvisioning}). The cursor driver registers a PIT when it opens
 * one; every search of the cursor then runs against it; the PIT is released when the cursor is exhausted, or when its
 * query is deleted or expires. A released PIT is gone at once, and {@link Handle#release} is idempotent.
 * <p>
 * A PIT holds one context per shard of its indices on the cluster, against {@code search.max_open_pit_context} (300
 * per node by default), so it's opened only on the indices the query reads, and kept only while the cursor has more
 * pages to give.
 */
public final class PointInTimes {
    private static final Map<String, Handle> BY_CURSOR = new ConcurrentHashMap<>();

    private PointInTimes() {
    }

    /** A cursor's PIT: its id, the keep-alive each search extends it by, and how to delete it. */
    public static final class Handle {
        private final String id;
        private final long keepAliveMillis;
        private final Runnable release;
        private volatile boolean released;

        public Handle(String id, long keepAliveMillis, Runnable release) {
            this.id = id;
            this.keepAliveMillis = keepAliveMillis;
            this.release = release;
        }

        public String getId() {
            return id;
        }

        public long getKeepAliveMillis() {
            return keepAliveMillis;
        }

        void release() {
            if (!released) {
                released = true;
                release.run();
            }
        }
    }

    public static String cursorContext(String queryId, String cursorId) {
        return queryId + "." + cursorId;
    }

    public static void register(String cursorContext, Handle handle) {
        Handle previous = BY_CURSOR.put(cursorContext, handle);
        if (previous != null && previous != handle) {
            previous.release();
        }
    }

    public static Optional<Handle> get(String cursorContext) {
        return cursorContext == null ? Optional.empty() : Optional.ofNullable(BY_CURSOR.get(cursorContext));
    }

    /** releases the PIT of one cursor, if it has one */
    public static void release(String cursorContext) {
        Handle handle = BY_CURSOR.remove(cursorContext);
        if (handle != null) {
            handle.release();
        }
    }

    /** releases the PITs of every cursor of a query (on delete or expiry) */
    public static void releaseQuery(String queryId) {
        String prefix = queryId + ".";
        BY_CURSOR.keySet().stream().filter(k -> k.startsWith(prefix)).forEach(PointInTimes::release);
    }

    /** how many PITs are registered (tests, diagnostics) */
    public static int size() {
        return BY_CURSOR.size();
    }
}
