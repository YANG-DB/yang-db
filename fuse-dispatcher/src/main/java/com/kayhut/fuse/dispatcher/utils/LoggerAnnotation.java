package com.kayhut.fuse.dispatcher.utils;

import com.codahale.metrics.Slf4jReporter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by liorp on 6/18/2017.
 */
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
public @interface LoggerAnnotation {
    enum Options {
        full,
        arguments,
        returnValue
    }

    Slf4jReporter.LoggingLevel logLevel();
    String name();
    Options options();
}
