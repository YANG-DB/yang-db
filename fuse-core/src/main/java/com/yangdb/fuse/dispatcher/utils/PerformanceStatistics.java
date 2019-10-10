package com.yangdb.fuse.dispatcher.utils;

/*-
 *
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Provider;
import com.yangdb.fuse.model.query.QueryMetadata;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by lior.perry on 6/18/2017.
 */
public class PerformanceStatistics implements MethodInterceptor {
    private Provider<MetricRegistry> metricRegistry;

    public PerformanceStatistics(Provider<MetricRegistry> metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {

        Class<?> declaringClass = ((Method) invocation.getStaticPart()).getDeclaringClass();
        String name = name(declaringClass);

        if (invocation.getArguments().length > 0) {
            Class<?> argumentClass = invocation.getArguments()[0].getClass();
            name = name(declaringClass,argumentClass.getSimpleName());
            if (invocation.getArguments()[0] instanceof QueryMetadata.QueryMetadataAble) {
                String id = ((QueryMetadata.QueryMetadataAble) invocation.getArguments()[0]).getQueryMetadata().getId();
                name = name(declaringClass,argumentClass.getSimpleName(), id);
            }
        }
        Timer.Context time = metricRegistry.get().timer(name).time();
        Object proceed = invocation.proceed();
        time.stop();
        return proceed;
    }
}
