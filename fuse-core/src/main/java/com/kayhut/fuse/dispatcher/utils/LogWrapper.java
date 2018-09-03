package com.kayhut.fuse.dispatcher.utils;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Created by liorp on 6/18/2017.
 */
//public class LogWrapper<D extends Descriptor> implements MethodInterceptor {

    /*private Provider<MetricRegistry> metricRegistry;
    private Map<Class, Descriptor> map;

    public LogWrapper(Provider<MetricRegistry> metricRegistry, Map<Class,Descriptor> map) {
        this.metricRegistry = metricRegistry;
        this.map = map;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        LoggerAnnotation annotation = null;
        for (Annotation annotation1 : invocation.getStaticPart().getAnnotations()) {
            if (annotation1 instanceof LoggerAnnotation) {
                annotation = (LoggerAnnotation) annotation1;
            }
        }

        Slf4jReporter.LoggingLevel level = annotation.logLevel();
        String annotationName = annotation.name();

        Class<?> declaringClass = ((Method) invocation.getStaticPart()).getDeclaringClass();
        String name = name(declaringClass, invocation.getMethod().getType());
        org.slf4j.Logger logger = LoggerFactory.getLogger(declaringClass);


        if (annotation.options() == LoggerAnnotation.Level.full || annotation.options() == LoggerAnnotation.Level.arguments) {
            for (int i = 0; i < invocation.getArguments().length; i++) {
                Descriptor descriptors = getDescriptor(invocation.getArguments()[i]);
                if (descriptors != null) {
                    String describe = descriptors.describe(invocation.getArguments()[i]);
                    String paramName = "Argument " + i;
                    reportParameter(logger, level, paramName, describe, annotationName);
                    Class<?> argumentClass = invocation.getArguments()[i].getClass();
                }
            }
        }

        Timer.Context time = metricRegistry.get().timer(name).time();
        Object proceed = invocation.proceed();
        time.stop();

        if (annotation.options() == LoggerAnnotation.Level.full || annotation.options() == LoggerAnnotation.Level.returnValue) {
            if (proceed != null) {
                String finalName = annotationName;
                if (proceed instanceof Iterable) {
                    ((Iterable) proceed).forEach(v -> reportProceed(level, logger, v, finalName));
                } else {
                    reportProceed(level, logger, proceed, finalName);
                }
            }
        }

        return proceed;
    }

    private void reportProceed(Slf4jReporter.LoggingLevel level, Logger logger, Object proceed, String annotationName) {
        Descriptor descriptors = getDescriptor(proceed);
        if(descriptors!=null) {
            String description = descriptors.describe(proceed);
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
        Descriptor descriptors = map.get(cls);
        return descriptors;
    }*/
//}
