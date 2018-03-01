package com.kayhut.test.generation;

import javaslang.collection.Stream;

import java.util.List;
import java.util.Random;

public class UniformFieldGenerator implements FieldGenerator {

    public UniformFieldGenerator(String fieldName, int maxNumValues, Random random, int start, int end) {
        this.fieldName = fieldName;
        this.maxNumValues = maxNumValues;
        this.random = random;
        this.start = start;
        this.end = end;
    }

    @Override
    public List<Integer> generateValues(){
        return Stream.range(0, random.nextInt(maxNumValues)+1)
                .map(i -> random.nextInt(end-start) + start).toJavaList();
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    private String fieldName;
    private int maxNumValues;
    private Random random;
    private int start;
    private int end;
}
