package com.yangdb.test;


import java.lang.annotation.*;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SleepySuite extends Suite {
    private final Logger log = LoggerFactory.getLogger(SleepySuite.class);
    private final Integer defaultSleepSec = 0;
    private final Integer sleepSec;

    public SleepySuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
        sleepSec = initSleep(klass);
    }

    private Integer initSleep(Class<?> klass) {
        SleepSec ts = klass.getAnnotation(SleepSec.class);
        Integer sleep = defaultSleepSec;
        if (ts != null) {
            sleep = ts.value();
            log.debug("Configured with sleep time: {}s", sleep);
        }
        return sleep;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface SleepSec {
        public int value();
    }

    /**
     * @see org.junit.runners.Suite#runChild(org.junit.runner.Runner, org.junit.runner.notification.RunNotifier)
     */
    @Override
    protected void runChild(Runner runner, RunNotifier notifier) {
        super.runChild(runner, notifier);
        //Simply wrapped Thread.sleep(long)
        try {
            System.out.println("sleep "+sleepSec);
            Thread.sleep(sleepSec*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}