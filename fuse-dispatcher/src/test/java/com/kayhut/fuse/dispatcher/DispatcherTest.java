package com.kayhut.fuse.dispatcher;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.kayhut.fuse.asg.AsgDriver;
import com.kayhut.fuse.epb.EpbDriver;
import com.kayhut.fuse.gta.GtaDriver;
import com.kayhut.fuse.model.process.GtaData;
import com.kayhut.fuse.model.process.QueryData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import static com.kayhut.fuse.model.Utils.submit;


public class DispatcherTest {

    protected Injector injector = Guice.createInjector(new AbstractModule() {

        @Override
        protected void configure() {
            bind(EventBus.class).toInstance(new EventBus());
            bind(DispatcherDriver.class);
            bind(AsgDriver.class).asEagerSingleton();
            bind(EpbDriver.class).asEagerSingleton();
            bind(GtaDriver.class).asEagerSingleton();
        }
    });

    private CountDownLatch latch = new CountDownLatch(1);

    @Before
    public void setup() {
        injector.injectMembers(this);
        //test listener for subscribing to last dto
        eventBus.register(this);
    }

    @Inject
    DispatcherDriver driver;
    @Inject
    EventBus eventBus;

    @Subscribe
    public void endProcess(GtaData input) {
        Assert.assertNotNull(input);
        latch.countDown();
    }

    @Test
    public void dispatcherFlow() throws InterruptedException {
        driver.process(new QueryData());
        latch.await();
        Assert.assertEquals(latch.getCount(),0);
    }

}
