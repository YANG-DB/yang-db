package com.kayhut.fuse.model.log;

import javaslang.Tuple2;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Created by liorp on 5/9/2017.
 */
public interface Trace<T> {
    void log(T event, Level level);

    List<Tuple2<String,T>> getLogs(Level level);

    String who();

    static Trace empty() {
        return EMPTY.instance;
    }

    static StringJoiner asString(Trace t, Level level, StringJoiner joiner) {
        t.getLogs(level).forEach(p->joiner.add(p.toString()));
        return joiner;
    }

    static <T> Trace<T> build(String who) {
        Level level = LogManager.getLogManager().getLogger("").getLevel();
        if(level.intValue() <= Level.INFO.intValue())
            return new TraceAble<>(who);
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

        @Override
        public String who() {
            return "EMPTY";
        }
    }
}
