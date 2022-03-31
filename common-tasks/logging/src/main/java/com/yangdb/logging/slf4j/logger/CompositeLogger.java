package com.yangdb.logging.slf4j.logger;

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

import org.slf4j.Logger;
import org.slf4j.Marker;

public class CompositeLogger implements Logger {
    //region Constructors
    public CompositeLogger(String name, Logger...loggers) {
        this.name = name;
        this.loggers = loggers;
    }
    //endregion

    //region Logger Implementation
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isTraceEnabled() {
        for(Logger logger : this.loggers) {
            if (logger.isTraceEnabled()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void trace(String s) {
        for(Logger logger : this.loggers) {
            logger.trace(s);
        }
    }

    @Override
    public void trace(String s, Object o) {
        for(Logger logger : this.loggers) {
            logger.trace(s, o);
        }
    }

    @Override
    public void trace(String s, Object o, Object o1) {
        for(Logger logger : this.loggers) {
            logger.trace(s, o, o1);
        }
    }

    @Override
    public void trace(String s, Object... objects) {
        for(Logger logger : this.loggers) {
            logger.trace(s, objects);
        }
    }

    @Override
    public void trace(String s, Throwable throwable) {
        for(Logger logger : this.loggers) {
            logger.trace(s, throwable);
        }
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        for(Logger logger : this.loggers) {
            if (logger.isTraceEnabled(marker)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void trace(Marker marker, String s) {
        for(Logger logger : this.loggers) {
            logger.trace(marker, s);
        }
    }

    @Override
    public void trace(Marker marker, String s, Object o) {
        for(Logger logger : this.loggers) {
            logger.trace(marker, s, o);
        }
    }

    @Override
    public void trace(Marker marker, String s, Object o, Object o1) {
        for(Logger logger : this.loggers) {
            logger.trace(marker, s, o, o1);
        }
    }

    @Override
    public void trace(Marker marker, String s, Object... objects) {
        for(Logger logger : this.loggers) {
            logger.trace(marker, s, objects);
        }
    }

    @Override
    public void trace(Marker marker, String s, Throwable throwable) {
        for(Logger logger : this.loggers) {
            logger.trace(marker, s, throwable);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        for(Logger logger : this.loggers) {
            if (logger.isDebugEnabled()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void debug(String s) {
        for(Logger logger : this.loggers) {
            logger.debug(s);
        }
    }

    @Override
    public void debug(String s, Object o) {
        for(Logger logger : this.loggers) {
            logger.debug(s, o);
        }
    }

    @Override
    public void debug(String s, Object o, Object o1) {
        for(Logger logger : this.loggers) {
            logger.debug(s, o, o1);
        }
    }

    @Override
    public void debug(String s, Object... objects) {
        for(Logger logger : this.loggers) {
            logger.debug(s, objects);
        }
    }

    @Override
    public void debug(String s, Throwable throwable) {
        for(Logger logger : this.loggers) {
            logger.debug(s, throwable);
        }
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        for(Logger logger : this.loggers) {
            if (logger.isDebugEnabled(marker)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void debug(Marker marker, String s) {
        for(Logger logger : this.loggers) {
            logger.debug(marker, s);
        }
    }

    @Override
    public void debug(Marker marker, String s, Object o) {
        for(Logger logger : this.loggers) {
            logger.debug(marker, s, o);
        }
    }

    @Override
    public void debug(Marker marker, String s, Object o, Object o1) {
        for(Logger logger : this.loggers) {
            logger.debug(marker, s, o, o1);
        }
    }

    @Override
    public void debug(Marker marker, String s, Object... objects) {
        for(Logger logger : this.loggers) {
            logger.debug(marker, s, objects);
        }
    }

    @Override
    public void debug(Marker marker, String s, Throwable throwable) {
        for(Logger logger : this.loggers) {
            logger.debug(marker, s, throwable);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        for(Logger logger : this.loggers) {
            if (logger.isInfoEnabled()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void info(String s) {
        for(Logger logger : this.loggers) {
            logger.info(s);
        }
    }

    @Override
    public void info(String s, Object o) {
        for(Logger logger : this.loggers) {
            logger.info(s, o);
        }
    }

    @Override
    public void info(String s, Object o, Object o1) {
        for(Logger logger : this.loggers) {
            logger.info(s, o, o1);
        }
    }

    @Override
    public void info(String s, Object... objects) {
        for(Logger logger : this.loggers) {
            logger.info(s, objects);
        }
    }

    @Override
    public void info(String s, Throwable throwable) {
        for(Logger logger : this.loggers) {
            logger.info(s, throwable);
        }
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        for(Logger logger : this.loggers) {
            if (logger.isInfoEnabled(marker)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void info(Marker marker, String s) {
        for(Logger logger : this.loggers) {
            logger.info(marker, s);
        }
    }

    @Override
    public void info(Marker marker, String s, Object o) {
        for(Logger logger : this.loggers) {
            logger.info(marker, s, o);
        }
    }

    @Override
    public void info(Marker marker, String s, Object o, Object o1) {
        for(Logger logger : this.loggers) {
            logger.info(marker, s, o, o1);
        }
    }

    @Override
    public void info(Marker marker, String s, Object... objects) {
        for(Logger logger : this.loggers) {
            logger.info(marker, s, objects);
        }
    }

    @Override
    public void info(Marker marker, String s, Throwable throwable) {
        for(Logger logger : this.loggers) {
            logger.info(marker, s, throwable);
        }
    }

    @Override
    public boolean isWarnEnabled() {
        for(Logger logger : this.loggers) {
            if (logger.isWarnEnabled()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void warn(String s) {
        for(Logger logger : this.loggers) {
            logger.warn(s);
        }
    }

    @Override
    public void warn(String s, Object o) {
        for(Logger logger : this.loggers) {
            logger.warn(s, o);
        }
    }

    @Override
    public void warn(String s, Object... objects) {
        for(Logger logger : this.loggers) {
            logger.warn(s, objects);
        }
    }

    @Override
    public void warn(String s, Object o, Object o1) {
        for(Logger logger : this.loggers) {
            logger.warn(s, o, o1);
        }
    }

    @Override
    public void warn(String s, Throwable throwable) {
        for(Logger logger : this.loggers) {
            logger.warn(s, throwable);
        }
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        for(Logger logger : this.loggers) {
            if (logger.isWarnEnabled(marker)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void warn(Marker marker, String s) {
        for(Logger logger : this.loggers) {
            logger.warn(marker, s);
        }
    }

    @Override
    public void warn(Marker marker, String s, Object o) {
        for(Logger logger : this.loggers) {
            logger.warn(marker, s, o);
        }
    }

    @Override
    public void warn(Marker marker, String s, Object o, Object o1) {
        for(Logger logger : this.loggers) {
            logger.warn(marker, s, o, o1);
        }
    }

    @Override
    public void warn(Marker marker, String s, Object... objects) {
        for(Logger logger : this.loggers) {
            logger.warn(marker, s, objects);
        }
    }

    @Override
    public void warn(Marker marker, String s, Throwable throwable) {
        for(Logger logger : this.loggers) {
            logger.warn(marker, s, throwable);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        for(Logger logger : this.loggers) {
            if (logger.isErrorEnabled()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void error(String s) {
        for(Logger logger : this.loggers) {
            logger.error(s);
        }
    }

    @Override
    public void error(String s, Object o) {
        for(Logger logger : this.loggers) {
            logger.error(s, o);
        }
    }

    @Override
    public void error(String s, Object o, Object o1) {
        for(Logger logger : this.loggers) {
            logger.error(s, o, o1);
        }
    }

    @Override
    public void error(String s, Object... objects) {
        for(Logger logger : this.loggers) {
            logger.error(s, objects);
        }
    }

    @Override
    public void error(String s, Throwable throwable) {
        for(Logger logger : this.loggers) {
            logger.error(s, throwable);
        }
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        for(Logger logger : this.loggers) {
            if (logger.isErrorEnabled(marker)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void error(Marker marker, String s) {
        for(Logger logger : this.loggers) {
            logger.error(marker, s);
        }
    }

    @Override
    public void error(Marker marker, String s, Object o) {
        for(Logger logger : this.loggers) {
            logger.error(marker, s, o);
        }
    }

    @Override
    public void error(Marker marker, String s, Object o, Object o1) {
        for(Logger logger : this.loggers) {
            logger.error(marker, s, o, o1);
        }
    }

    @Override
    public void error(Marker marker, String s, Object... objects) {
        for(Logger logger : this.loggers) {
            logger.error(marker, s, objects);
        }
    }

    @Override
    public void error(Marker marker, String s, Throwable throwable) {
        for(Logger logger : this.loggers) {
            logger.error(marker, s, throwable);
        }
    }
    //endregion

    //region Fields
    private String name;
    private Logger[] loggers;
    //endregion
}
