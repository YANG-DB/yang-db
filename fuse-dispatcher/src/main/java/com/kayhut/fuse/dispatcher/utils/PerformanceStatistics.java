package com.kayhut.fuse.dispatcher.utils;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.inject.Provider;
import com.kayhut.fuse.model.query.QueryMetadata;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by liorp on 6/18/2017.
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
