package com.kayhut.fuse.model.log;

import javaslang.Tuple2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by liorp on 5/9/2017.
 */
public class TraceCompositeAble<T> extends TraceAble<T> implements TraceComposite<T>{
    private List<Trace<T>> traces;

    public TraceCompositeAble(String who)    {
        super(who);
        traces = new ArrayList<>();
    }

    @Override
    public List<Tuple2<String,T>> getLogs(Level level) {
        return traces.stream().map(t -> t.getLogs(level))
                .filter(log -> !log.isEmpty())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void with(Trace<T> trace) {
        traces.add(trace);
    }
}
