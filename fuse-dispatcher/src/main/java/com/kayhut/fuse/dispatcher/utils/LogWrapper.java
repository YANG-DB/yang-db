package com.kayhut.fuse.dispatcher.utils;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import com.google.inject.Provider;
import com.kayhut.fuse.model.descriptor.Descriptor;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by liorp on 6/18/2017.
 */
public class LogWrapper<D extends Descriptor> implements MethodInterceptor {

    private Provider<MetricRegistry> metricRegistry;
    private Map<Class, Descriptor> map;

    public LogWrapper(Provider<MetricRegistry> metricRegistry, Map<Class,Descriptor> map) {
        this.metricRegistry = metricRegistry;
        this.map = map;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        LoggerAnnotation annotation = null;
        for (Annotation annotation1 : invocation.getStaticPart().getAnnotations()) {
            if(annotation1 instanceof LoggerAnnotation)
                annotation = (LoggerAnnotation)annotation1;
        }

        Slf4jReporter.LoggingLevel level = annotation.logLevel();
        String annotationName = annotation.name();

        Class<?> declaringClass = ((Method) invocation.getStaticPart()).getDeclaringClass();
        String name = name(declaringClass, invocation.getMethod().getName());
        org.slf4j.Logger logger = LoggerFactory.getLogger(declaringClass);


        for (int i = 0; i < invocation.getArguments().length; i++) {
            Descriptor descriptor = getDescriptor(invocation.getArguments()[i]);
            if(descriptor!=null) {
                String describe = descriptor.describe(invocation.getArguments()[i]);
                String paramName = "Argument " + i;
                reportParameter(logger, level, paramName, describe, annotationName);
                Class<?> argumentClass = invocation.getArguments()[i].getClass();
                name = name(declaringClass, argumentClass.getSimpleName());
            }
        }

        Timer.Context time = metricRegistry.get().timer(name).time();
        Object proceed = invocation.proceed();
        time.stop();

        if (proceed != null) {
            String finalName = annotationName;
            if(proceed instanceof Iterable) {
                ((Iterable) proceed).forEach(v-> reportProceed(level, logger, v, finalName));
            }else {
                reportProceed(level, logger, proceed, finalName);
            }
        }

        return proceed;
    }

    private void reportProceed(Slf4jReporter.LoggingLevel level, Logger logger, Object proceed, String annotationName) {
        Descriptor descriptor = getDescriptor(proceed);
        if(descriptor!=null) {
            String description = descriptor.describe(proceed);
            String message = "[annotationName:{}][return-value:{}]";
            List<String> args = new ArrayList<>(4);
            args.add(annotationName);
            args.add(description);
            if(NDC.getDepth() > 0){
                args.add((String) NDC.cloneStack().stream().map(p->((NDC.DiagnosticContext) p).fullMessage).collect(Collectors.joining(", ")));
                message += "[context:{}]";
            }
            report(logger, level, message, args.toArray());
        }
    }

    private void reportParameter(Logger logger, Slf4jReporter.LoggingLevel level, String paramName, String description, String annotationName) {
        String message = "[annotationName:{}][paramName:{}][value:{}]";
        List<String> args = new ArrayList<>(4);
        args.add(annotationName);
        args.add(paramName);
        args.add(description);
        if(NDC.getDepth() > 0){
            args.add((String) NDC.cloneStack().stream().map(p->((NDC.DiagnosticContext) p).fullMessage).collect(Collectors.joining(", ")));
            message += "[context:{}]";
        }
        report(logger, level, message, args.toArray());
    }

    private void report(Logger logger, Slf4jReporter.LoggingLevel level, String message, Object[] args){
        switch (level) {
            case INFO:
                logger.info(message, args);
                break;
            case DEBUG:
                logger.debug(message, args);
                break;
            case ERROR:
                logger.error(message, args);
                break;
            case TRACE:
                logger.trace(message, args);
                break;
            case WARN:
                logger.warn(message, args);
                break;

        }
    }

    private Descriptor getDescriptor(Object object){
        Class cls = object.getClass();
        Descriptor descriptor = map.get(cls);
        return descriptor;
    }
}
