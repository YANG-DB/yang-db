package com.kayhut.fuse.model.log;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;

/**
 * Created by liorp on 5/11/2017.
 */
public interface TraceComposite<T> extends Trace<T> {
    void with(Trace<T> trace);

    static <T> TraceComposite<T> build(String who) {
        Level level = LogManager.getLogManager().getLogger("").getLevel();
        if (level.intValue() <= Level.INFO.intValue())
            return new TraceCompositeAble<>(who);
        //todo check different log levels
        return empty();
    }

    static TraceComposite empty() {
        return EMPTY1.instance;
    }

    class EMPTY1 implements TraceComposite{
        public static final EMPTY1 instance = new EMPTY1();

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

        @Override
        public void with(Trace trace) {

        }
    }
}
