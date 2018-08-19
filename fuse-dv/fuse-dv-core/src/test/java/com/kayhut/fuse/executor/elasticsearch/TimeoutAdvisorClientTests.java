package com.kayhut.fuse.executor.elasticsearch;

import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.executor.BaseModuleInjectionTest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.Client;
import org.jooby.internal.RequestScope;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeoutException;


public class TimeoutAdvisorClientTests extends BaseModuleInjectionTest {


    @Test
    public void testClientTimeout() {
        init(new ModuleBase[]{new TestExecutorModule(1000,200)});
        setup();

        Client instance = injector.getInstance(Client.class);
        try {
            instance.search(new SearchRequest());
        } catch (RuntimeException e) {
            if (e.getCause() instanceof TimeoutException) {
                Assert.assertTrue("Timeout exception should not have occurred", true);
                return;
            }
        }
        Assert.assertTrue(true);
    }

    @Test
    public void testClientNoTimeout() {
        init(new ModuleBase[]{new TestExecutorModule(100,200)});
        setup();

        Client instance = injector.getInstance(Client.class);
        try {
            instance.search(new SearchRequest());
        } catch (RuntimeException e) {
            if (e.getCause() instanceof TimeoutException) {
                Assert.assertTrue(true);
                return;
            } else {
                Assert.assertTrue("Timeout exception should have occurred", false);
                return;
            }
        }
        Assert.assertTrue("Timeout exception should have occurred", false);
    }
}
