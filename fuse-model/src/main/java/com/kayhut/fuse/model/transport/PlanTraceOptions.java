package com.kayhut.fuse.model.transport;

/**
 * Created by Roman on 12/23/2017.
 */
public class PlanTraceOptions {
    //region Level
    public enum Level {
        none,
        info,
        debug,
        trace,
        verbose
    }
    //endregion

    //region Static Methods
    public static PlanTraceOptions of(Level level) {
        PlanTraceOptions planTraceOptions = new PlanTraceOptions();
        planTraceOptions.setLevel(level);
        return planTraceOptions;
    }
    //endregion

    //region Constructors
    public PlanTraceOptions() {

    }
    //endregion

    //region Properties
    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
    //endregion

    //region Fields
    private Level level;
    //endregion
}
