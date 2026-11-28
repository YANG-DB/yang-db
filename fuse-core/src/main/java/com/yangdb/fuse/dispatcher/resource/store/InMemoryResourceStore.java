package com.yangdb.fuse.dispatcher.resource.store;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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



import com.yangdb.fuse.dispatcher.resource.CursorResource;
import com.yangdb.fuse.dispatcher.resource.PageResource;
import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.model.transport.CreateQueryRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Volatile (in-memory) store of queries, with their cursors and fetched pages.
 * <p>
 * A query is removed once it hasn't been used for longer than its ttl ({@code CreateQueryRequest.ttl}, default 5
 * minutes; 0 or less keeps it until it's deleted). Using a query, one of its cursors or pages, or a composite query
 * that contains it, counts as use. A background check runs every {@link #CHECK_INTERVAL_MS}.
 * <p>
 * Before Phase 7.7 the ttl was never enforced: every query a client didn't delete stayed in memory for good, and a
 * stored query with a large page holds tens of MB (Q9 at 10x the tutorial data: about 54 MB), so a few dozen ran the
 * engine out of heap (Q14 in docs/revival/KNOWN_ISSUES.md).
 */
public class InMemoryResourceStore implements ResourceStore {
    static final long CHECK_INTERVAL_MS = 30_000;

    private static final ScheduledExecutorService EXPIRY = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "query-resource-expiry");
        t.setDaemon(true);
        return t;
    });

    //region Constructors
    public InMemoryResourceStore() {
        this(System::currentTimeMillis);
        EXPIRY.scheduleWithFixedDelay(this::removeExpiredQuietly, CHECK_INTERVAL_MS, CHECK_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /** for tests: a store with its own clock and no background check (call {@link #removeExpired()}) */
    InMemoryResourceStore(LongSupplier clock) {
        this.queryResources = new ConcurrentHashMap<>();
        this.lastUsed = new ConcurrentHashMap<>();
        this.clock = clock;
    }
    //endregion

    //region ResourceStore Implementation
    @Override
    public Collection<QueryResource> getQueryResources() {
        return new ArrayList<>(this.queryResources.values());
    }

    @Override
    public Collection<QueryResource> getQueryResources(Predicate<String> predicate) {
        return this.queryResources.entrySet().stream()
                .filter(e->predicate.test(e.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<QueryResource> getQueryResource(String id) {
        QueryResource resource = this.queryResources.get(id);
        if (resource != null) {
            used(resource);
        }
        return Optional.ofNullable(resource);
    }

    @Override
    public Optional<CursorResource> getCursorResource(String queryId, String cursorId) {
        Optional<QueryResource> queryResource = this.getQueryResource(queryId);
        return queryResource.flatMap(queryResource1 -> queryResource1.getCursorResource(cursorId));
    }

    @Override
    public Optional<PageResource> getPageResource(String queryId, String cursorId, String pageId) {
        Optional<CursorResource> cursorResource = this.getCursorResource(queryId, cursorId);
        return cursorResource.flatMap(cursorResource1 -> cursorResource1.getPageResource(pageId));
    }

    @Override
    public boolean addQueryResource(QueryResource queryResource) {
        this.queryResources.put(queryResource.getQueryMetadata().getId(), queryResource);
        used(queryResource);
        return true;
    }

    @Override
    public boolean deleteQueryResource(String queryId) {
        this.lastUsed.remove(queryId);
        // a consistent cursor's Point in Time (ROADMAP 7.8) goes with its query, on delete and on expiry
        com.yangdb.fuse.dispatcher.provision.PointInTimes.releaseQuery(queryId);
        return this.queryResources.remove(queryId)!=null;
    }
    @Override
    public boolean addCursorResource(String queryId, CursorResource cursorResource) {
        Optional<QueryResource> queryResource = this.getQueryResource(queryId);
        if (queryResource.isPresent()) {
            queryResource.get().addCursorResource(cursorResource.getCursorId(), cursorResource);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteCursorResource(String queryId, String cursorId) {
        Optional<QueryResource> queryResource = this.getQueryResource(queryId);
        if (queryResource.isPresent()) {
            queryResource.get().deleteCursorResource(cursorId);
            return true;
        }
        return false;
    }

    @Override
    public boolean addPageResource(String queryId, String cursorId, PageResource pageResource) {
        Optional<QueryResource> queryResource = this.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return false;
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (cursorResource.isPresent()) {
            cursorResource.get().addPageResource(pageResource.getPageId(), pageResource);
        }
        return true;
    }

    @Override
    public boolean deletePageResource(String queryId, String cursorId, String pageId) {
        Optional<QueryResource> queryResource = this.getQueryResource(queryId);
        if (!queryResource.isPresent()) {
            return false;
        }

        Optional<CursorResource> cursorResource = queryResource.get().getCursorResource(cursorId);
        if (cursorResource.isPresent()) {
            cursorResource.get().deletePageResource(pageId);
        }
        return true;
    }
    //endregion

    //region Expiry
    /** removes every query that hasn't been used for longer than its ttl; returns how many */
    int removeExpired() {
        long now = this.clock.getAsLong();
        List<String> expired = this.queryResources.entrySet().stream()
                .filter(e -> {
                    long ttl = e.getValue().getQueryMetadata().getTtl();
                    Long last = this.lastUsed.get(e.getKey());
                    return ttl > 0 && last != null && now - last > ttl;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        expired.forEach(this::deleteQueryResource);
        return expired.size();
    }

    private void removeExpiredQuietly() {
        try {
            removeExpired();
        } catch (RuntimeException e) {
            // keep the schedule alive; the next check tries again
        }
    }

    /** marks a query, and the inner queries of a composite one, as used now */
    private void used(QueryResource resource) {
        long now = this.clock.getAsLong();
        this.lastUsed.put(resource.getQueryMetadata().getId(), now);
        for (QueryResource inner : resource.getInnerQueryResources()) {
            if (inner != resource) {
                used(inner);
            }
        }
    }
    //endregion

    @Override
    public boolean test(CreateQueryRequest.StorageType type) {
        return type.equals(CreateQueryRequest.StorageType._volatile);
    }

//endregion

    //region Fields
    private final Map<String, QueryResource> queryResources;
    private final Map<String, Long> lastUsed;
    private final LongSupplier clock;
    //endregion
}
