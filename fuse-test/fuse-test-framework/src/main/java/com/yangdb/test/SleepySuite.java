package com.yangdb.test;

/*-
 * #%L
 * fuse-test-framework
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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
