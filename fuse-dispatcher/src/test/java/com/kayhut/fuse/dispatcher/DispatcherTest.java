package com.kayhut.fuse.dispatcher;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import com.kayhut.fuse.dispatcher.driver.QueryDispatcherDriver;
import com.kayhut.fuse.dispatcher.driver.SimpleQueryDispatcherDriver;
import com.typesafe.config.Config;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

@Ignore
public class DispatcherTest {

    protected Injector injector = Guice.createInjector(new AbstractModule() {

        @Override
        protected void configure() {
            bind(EventBus.class).toInstance(new EventBus());
            bind(QueryDispatcherDriver.class).to(SimpleQueryDispatcherDriver.class);
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
    @Inject
    Config config;

    @Test
    public void dispatcherFlow() throws InterruptedException {
       /* driver.process(new QueryData(new QueryMetadata("a","b","c",System.currentTimeMillis()),new Query()));
        latch.await();
        Assert.assertEquals(latch.getCount(),0);*/
    }

}
