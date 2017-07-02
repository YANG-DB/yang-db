package com.kayhut.fuse.dispatcher.utils;

import com.codahale.metrics.Slf4jReporter;

/**
 * Created by Roman on 28/06/2017.
 */
public class LoggerAnnotationInstance {
    //region Constructors
    public LoggerAnnotationInstance(Slf4jReporter.LoggingLevel level, String name, LoggerAnnotation.Options options) {
        this.level = level;
        this.name = name;
        this.options = options;
    }
    //endregion

    //region Properties
    public Slf4jReporter.LoggingLevel getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public LoggerAnnotation.Options getOptions() {
        return options;
    }
    //endregion

    //region Fields
    private Slf4jReporter.LoggingLevel level;
    private String name;
    private LoggerAnnotation.Options options;
    //endregion
}
