package com.kayhut.fuse.epb.plan.validation;

import com.kayhut.fuse.epb.plan.PlanValidator;
import com.kayhut.fuse.model.log.Trace;
import javaslang.Tuple2;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by moti on 2/23/2017.
 */
public class DummyValidator<P,Q> implements PlanValidator<P,Q> {
    @Override
    public Trace<String> clone() {
        return this;
    }
    @Override
    public boolean isPlanValid(P plan, Q query) {
        return true;
    }

    @Override
    public void log(String event, Level level) {}

    @Override
    public List<Tuple2<String,String>> getLogs(Level level) {
        return Collections.emptyList();
    }

    @Override
    public String who() {
        return DummyValidator.class.getSimpleName();
    }
}
