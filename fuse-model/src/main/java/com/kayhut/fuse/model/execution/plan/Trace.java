package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.TraceAble;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Created by liorp on 5/9/2017.
 */
public interface Trace<T> {
    void log(T event, Level level);

    List<T> getLogs(Level level);

    static EMPTY empty() {
        return EMPTY.instance;
    }

    static <T> Trace<T> build() {
        Level level = LogManager.getLogManager().getLogger("").getLevel();
        if(level.intValue() <= Level.INFO.intValue())
            return new TraceAble<>();
        //todo check different log levels
        return empty();
    }

    class EMPTY implements Trace{
        public static final EMPTY instance = new EMPTY();

        @Override
        public void log(Object event, Level level) {}

        @Override
        public List getLogs(Level level) {
            return Collections.emptyList();
        }
    }
}
