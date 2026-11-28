package com.yangdb.fuse.dispatcher.provision;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class PointInTimesTest {

    @Test
    public void aCursorsPointInTimeIsReleasedOnceAndForgotten() {
        AtomicInteger deletes = new AtomicInteger();
        String cursor = PointInTimes.cursorContext("qA", "1");
        PointInTimes.register(cursor, new PointInTimes.Handle("p", 1000, deletes::incrementAndGet));
        Assert.assertEquals("p", PointInTimes.get(cursor).get().getId());
        PointInTimes.release(cursor);
        PointInTimes.release(cursor);
        Assert.assertEquals(1, deletes.get());
        Assert.assertFalse(PointInTimes.get(cursor).isPresent());
    }

    @Test
    public void deletingAQueryReleasesTheTimesOfAllItsCursorsAndNoOthers() {
        AtomicInteger deletes = new AtomicInteger();
        PointInTimes.register(PointInTimes.cursorContext("qB", "1"), new PointInTimes.Handle("p1", 1000, deletes::incrementAndGet));
        PointInTimes.register(PointInTimes.cursorContext("qB", "2"), new PointInTimes.Handle("p2", 1000, deletes::incrementAndGet));
        PointInTimes.register(PointInTimes.cursorContext("qBB", "1"), new PointInTimes.Handle("p3", 1000, () -> { }));
        PointInTimes.releaseQuery("qB");
        Assert.assertEquals(2, deletes.get());
        Assert.assertTrue(PointInTimes.get(PointInTimes.cursorContext("qBB", "1")).isPresent());
        PointInTimes.releaseQuery("qBB");
    }

    @Test
    public void noContextMeansNoPointInTime() {
        Assert.assertFalse(PointInTimes.get(null).isPresent());
        Assert.assertFalse(PointInTimes.get("not-registered").isPresent());
    }
}
