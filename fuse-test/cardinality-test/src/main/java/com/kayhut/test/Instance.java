package com.kayhut.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Instance {

    public Instance(int instanceId) {
        this.instanceId = instanceId;
    }

    public void addValues(String field, List<Integer> values){
        this.values.put(field, values);
    }

    public Map<String, List<Integer>> getValues() {
        return values;
    }

    public int getInstanceId() {
        return instanceId;
    }

    Map<String, List<Integer>> values = new HashMap<>();
    private int instanceId;
}
