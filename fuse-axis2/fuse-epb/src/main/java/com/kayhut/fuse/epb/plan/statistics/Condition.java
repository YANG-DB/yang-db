package com.kayhut.fuse.epb.plan.statistics;

/**
 * Created by moti on 4/18/2017.
 */
public class Condition {
    private String property;
    private String conditionType;
    private Object value;

    public Condition(String property, String conditionType, Object value) {
        this.property = property;
        this.conditionType = conditionType;
        this.value = value;
    }

    public String getProperty() {
        return property;
    }

    public String getConditionType() {
        return conditionType;
    }

    public Object getValue() {
        return value;
    }
}
