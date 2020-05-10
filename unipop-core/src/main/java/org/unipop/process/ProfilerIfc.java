package org.unipop.process;

public interface ProfilerIfc {
    default Profiler getProfiler() {
        return Profiler.Noop.instance;
    }

    default void setProfiler(Profiler profiler) { }

}
