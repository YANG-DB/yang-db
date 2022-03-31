package com.yangdb.logging;

/*-
 * #%L
 * logging
 * %%
 * Copyright (C) 2016 - 2022 The YangDb Graph Database Project
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

import com.yangdb.logging.mdc.MDCWriter;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    LogMessage withMarker(Marker marker);

    class Noop implements LogMessage {
        //region LogMessage Implementation
        @Override
        public void log() {

        }

        @Override
        public LogMessage with(Object... args) {
            return null;
        }

        @Override
        public LogMessage withMarker(Marker marker) {
            return null;
        }
        //endregion
    }

    class MDC implements LogMessage {
        //region Constructors
        public MDC(Logger logger, Level level, String message, MDCWriter...mdcWriters) {
            this.logger = logger;
            this.level = level;
            this.message = message;
            this.args = Collections.emptyList();
            this.mdcWriters = mdcWriters;
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

            for(MDCWriter mdcWriter : this.mdcWriters) {
                mdcWriter.write();
            }

            switch (this.level) {
                case trace:
                    this.logger.trace(this.marker, this.message, toArray(this.args));
                    break;
                case debug:
                    this.logger.debug(this.marker, this.message, toArray(this.args));
                    break;
                case info:
                    this.logger.info(this.marker, this.message, toArray(this.args));
                    break;
                case warn:
                    this.logger.warn(this.marker, this.message, toArray(this.args));
                    break;
                case error:
                    this.logger.error(this.marker, this.message, toArray(this.args));
                    break;
            }
        }

        @Override
        public MDC with(Object...args) {
            if (this.args.isEmpty()) {
                this.args = new ArrayList<>();
            }

            this.args.addAll(Arrays.asList(args));
            return this;
        }

        @Override
        public LogMessage withMarker(Marker marker) {
            this.marker = marker;
            return this;
        }
        //endregion

        //region Private Methods
        private Object[] toArray(List<Object> objects) {
            Object[] objectArray = new Object[objects.size()];
            for(int i = 0 ; i < objectArray.length ; i++) {
                objectArray[i] = objects.get(i);
            }
            return objectArray;
        }
        //endregion

        //region Fields
        private String message;
        private List<Object> args;
        private org.slf4j.Logger logger;
        private Level level;
        private Marker marker;
        private MDCWriter[] mdcWriters;
        //endregion
    }
}
