package com.kayhut.test.generation;

import com.kayhut.test.Instance;

import java.util.List;

public class InstanceGenerator {
    public InstanceGenerator(List<FieldGenerator> fieldGenerators, IdGenerator idGenerator) {
        this.fieldGenerators = fieldGenerators;
        this.idGenerator = idGenerator;
    }

    public Instance generateInstance(){
        Instance instance = new Instance(this.idGenerator.nextId());


        for (FieldGenerator fieldGenerator : this.fieldGenerators) {
            instance.addValues(fieldGenerator.getFieldName(), fieldGenerator.generateValues());
        }
        return instance;
    }

    private List<FieldGenerator> fieldGenerators;
    private IdGenerator idGenerator;

}
