package com.kayhut.test;

import java.util.List;

public class InstanceGenerator {
    public InstanceGenerator(List<FieldGenerator> fieldGenerators) {
        this.fieldGenerators = fieldGenerators;
    }

    public Instance generateInstance(){
        Instance instance = new Instance(this.nextInstanceId);
        this.nextInstanceId++;

        for (FieldGenerator fieldGenerator : this.fieldGenerators) {
            instance.addValues(fieldGenerator.getFieldName(), fieldGenerator.generateValues());
        }
        return instance;
    }

    private int nextInstanceId = 1;
    private List<FieldGenerator> fieldGenerators;

}
