package com.kayhut.fuse.model.log;

import javaslang.Tuple2;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by liorp on 5/9/2017.
 */
public class TraceAble<T> implements Trace<T>{
    private List<Tuple2<Level,T>> log;
    private String who;

    public TraceAble(String who) {
        this.who = who;
        log = new ArrayList<>();
    }

    @Override
    public void log(T event, Level level) {
        log.add(new Tuple2<>(level,event));
    }

    @Override
    public List<Tuple2<String,T>> getLogs(Level level) {
        return log.stream().filter(log->log._1.intValue()<=level.intValue()).map(v->new Tuple2<>(who,v._2)).collect(Collectors.toList());
    }

    @Override
    public String who() {
        return who;
    }
}
