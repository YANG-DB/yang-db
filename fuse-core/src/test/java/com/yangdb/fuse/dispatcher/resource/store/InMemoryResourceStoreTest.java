package com.yangdb.fuse.dispatcher.resource.store;

import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.model.query.QueryMetadata;
import com.yangdb.fuse.model.transport.CreateQueryRequest;
import org.junit.Assert;
import org.junit.Test;

/** Phase 7.7: queries expire after their ttl without use (before, the ttl was never enforced: Q14). */
public class InMemoryResourceStoreTest {
    private long now = 1_000_000;
    private final InMemoryResourceStore store = new InMemoryResourceStore(() -> now);

    private static QueryResource query(String id, long ttl) {
        QueryMetadata metadata = new QueryMetadata(CreateQueryRequest.StorageType._volatile, id, id, false, 0, ttl);
        return new QueryResource(null, null, null, metadata, null);
    }

    @Test
    public void unusedQueryExpiresAfterItsTtl() {
        store.addQueryResource(query("q", 300_000));
        now += 300_000;
        Assert.assertEquals(0, store.removeExpired());
        now += 1;
        Assert.assertEquals(1, store.removeExpired());
        Assert.assertFalse(store.getQueryResource("q").isPresent());
    }

    @Test
    public void usingAQueryKeepsItAlive() {
        store.addQueryResource(query("q", 300_000));
        for (int i = 0; i < 5; i++) {
            now += 200_000; // a client paging slowly, but within the ttl each time
            Assert.assertTrue(store.getQueryResource("q").isPresent());
            Assert.assertEquals(0, store.removeExpired());
        }
        now += 300_001;
        Assert.assertEquals(1, store.removeExpired());
    }

    @Test
    public void ttlOfZeroNeverExpires() {
        store.addQueryResource(query("q", 0));
        now += 1_000_000_000L;
        Assert.assertEquals(0, store.removeExpired());
        Assert.assertTrue(store.getQueryResource("q").isPresent());
    }

    @Test
    public void usingACompositeQueryKeepsItsInnerQueriesAlive() {
        QueryResource inner = query("inner", 300_000);
        QueryResource outer = query("outer", 300_000);
        outer.addInnerQueryResource(inner);
        store.addQueryResource(inner);
        store.addQueryResource(outer);
        now += 250_000;
        store.getQueryResource("outer");
        now += 250_000;
        Assert.assertEquals(0, store.removeExpired());
        Assert.assertTrue(store.getQueryResource("inner").isPresent());
    }

    @Test
    public void deletedQueryIsGone() {
        store.addQueryResource(query("q", 300_000));
        Assert.assertTrue(store.deleteQueryResource("q"));
        now += 300_001;
        Assert.assertEquals(0, store.removeExpired());
    }
}
