package com.kayhut.fuse.dispatcher.logging;

import javaslang.collection.Stream;
import org.slf4j.Logger;

import java.util.List;

import static ch.qos.logback.classic.Level.TRACE;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LogMessage {
    public enum Level {
        trace,
        debug,
        info,
        warn,
        error
    }

    //region Constructors
    public LogMessage(Logger logger, Level level, String message, Object...args) {
        this.logger = logger;
        this.level = level;
        this.message = message;
        this.args = Stream.of(args);
    }
    //endregion

    //region Public Methods
    public void log() {
        switch (level) {
            case trace: this.logger.trace(this.message, this.args.toJavaArray());
                break;
            case debug: this.logger.debug(this.message, this.args.toJavaArray());
                break;
            case info: this.logger.info(this.message, this.args.toJavaArray());
                break;
            case warn: this.logger.warn(this.message, this.args.toJavaArray());
                break;
            case error: this.logger.error(this.message, this.args.toJavaArray());
                break;
        }
    }

    public LogMessage with(Object arg) {
        this.args.append(arg);
        return this;
    }
    //endregion

    //region Fields
    private String message;
    private Stream<Object> args;
    private Logger logger;
    private Level level;
    //endregion
}
