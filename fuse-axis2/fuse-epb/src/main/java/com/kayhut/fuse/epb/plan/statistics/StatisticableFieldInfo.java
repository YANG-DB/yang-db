package com.kayhut.fuse.epb.plan.statistics;

/**
 * Created by moti on 31/03/2017.
 */
public class StatisticableFieldInfo {
    private String name;
    private String type;

    public StatisticableFieldInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
