package org.unipop.query.controller;

import org.unipop.process.Profiler;

public interface UniQueryController {
    default Profiler getProfiler() {
        return Profiler.Noop.instance;
    }

    default void setProfiler(Profiler profiler) { }
}
