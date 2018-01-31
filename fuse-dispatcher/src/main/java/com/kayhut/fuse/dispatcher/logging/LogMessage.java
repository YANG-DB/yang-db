package com.kayhut.fuse.dispatcher.logging;

import javaslang.collection.Stream;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.List;
import java.util.Optional;

import static ch.qos.logback.classic.Level.TRACE;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public interface LogMessage {
    Noop noop = new Noop();

    enum Level {
        trace,
        debug,
        info,
        warn,
        error
    }

    void log();
    LogMessage with(Object...args);

    class Noop implements LogMessage {
        //region LogMessage Implementation
        @Override
        public void log() {

        }

        @Override
        public LogMessage with(Object... args) {
            return null;
        }
        //endregion
    }

    interface MDCWriter {
        void write();

        class KeyValue implements MDCWriter {
            //region Constructors
            public KeyValue(String key, String value) {
                this.key = key;
                this.value =value;
            }
            //endregion

            //region MDCProp Implementation
            @Override
            public void write() {
                MDC.put(this.key, this.value);
            }
            //endregion

            //region Fields
            private String key;
            private String value;
            //endregion
        }
    }

    class Impl implements LogMessage {
        //region Constructors
        public Impl(Logger logger, Level level, String message, MDCWriter...mdcWriters) {
            this.logger = logger;
            this.level = level;
            this.message = message;
            this.args = Stream.empty();
            this.mdcWriters = Stream.of(mdcWriters);
        }
        //endregion

        //region Public Methods
        @Override
        public void log() {
            switch (this.level) {
                case trace: if (!this.logger.isTraceEnabled()) return;
                    break;
                case debug: if (!this.logger.isDebugEnabled()) return;
                    break;
                case info: if (!this.logger.isInfoEnabled()) return;
                    break;
                case warn: if (!this.logger.isWarnEnabled()) return;
                    break;
                case error: if (!this.logger.isErrorEnabled()) return;
                    break;
            }

            this.mdcWriters.forEach(MDCWriter::write);

            switch (this.level) {
                case trace:
                    this.logger.trace(this.message, this.args.toJavaArray());
                    break;
                case debug:
                    this.logger.debug(this.message, this.args.toJavaArray());
                    break;
                case info:
                    this.logger.info(this.message, this.args.toJavaArray());
                    break;
                case warn:
                    this.logger.warn(this.message, this.args.toJavaArray());
                    break;
                case error:
                    this.logger.error(this.message, this.args.toJavaArray());
                    break;
            }
        }

        @Override
        public LogMessage with(Object...args) {
            this.args = this.args.appendAll(Stream.of(args));
            return this;
        }
        //endregion

        //region Fields
        private String message;
        private Stream<Object> args;
        private Logger logger;
        private Level level;
        private Iterable<MDCWriter> mdcWriters;
        //endregion
    }
}
