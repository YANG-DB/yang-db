package com.kayhut.fuse.dispatcher;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.kayhut.fuse.asg.BaseAsgDriver;
import com.kayhut.fuse.epb.BaseEpbDriver;
import com.kayhut.fuse.gta.BaseGtaDriver;
import com.kayhut.fuse.model.process.*;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.QueryMetadata;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;


public class DispatcherTest {

    protected Injector injector = Guice.createInjector(new AbstractModule() {

        @Override
        protected void configure() {
            bind(EventBus.class).toInstance(new EventBus());
            bind(QueryDispatcherDriver.class).to(BaseQueryDispatcherDriver.class);
            bind(BaseAsgDriver.class).asEagerSingleton();
            bind(BaseEpbDriver.class).asEagerSingleton();
            bind(BaseGtaDriver.class).asEagerSingleton();
        }
    });

    private CountDownLatch latch = new CountDownLatch(3);

    @Before
    public void setup() {
        injector.injectMembers(this);
        //test listener for subscribing to last dto
        eventBus.register(this);
    }

    @Inject
    QueryDispatcherDriver driver;
    @Inject
    EventBus eventBus;

    @Subscribe
    public void asgDriverProcess(AsgData input) {
        Assert.assertNotNull(input);
        System.out.println("Asg Driver proccess passed");
        latch.countDown();
    }

    @Subscribe
    public void epbDriverProcess(EpbData input) {
        Assert.assertNotNull(input);
        System.out.println("Epb Driver proccess passed");
        latch.countDown();
    }
    @Subscribe
    public void gtaDriverProcess(GtaData input) {
        Assert.assertNotNull(input);
        System.out.println("Gta Driver proccess passed");
        latch.countDown();
    }

    @Test
    public void dispatcherFlow() throws InterruptedException {
       /* driver.process(new QueryData(new QueryMetadata("a","b","c",System.currentTimeMillis()),new Query()));
        latch.await();
        Assert.assertEquals(latch.getCount(),0);*/
    }

}
