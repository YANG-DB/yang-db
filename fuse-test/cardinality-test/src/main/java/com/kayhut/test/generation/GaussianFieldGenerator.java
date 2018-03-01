package com.kayhut.test.generation;

import javaslang.collection.Stream;

import java.util.List;
import java.util.Random;

public class GaussianFieldGenerator implements FieldGenerator {

    public GaussianFieldGenerator(String fieldName, int maxNumValues, Random random, int start, int end, int mean, double std) {
        this.fieldName = fieldName;
        this.maxNumValues = maxNumValues;
        this.random = random;
        this.start = start;
        this.end = end;
        this.mean = mean;
        this.std = std;
    }

    @Override
    public List<Integer> generateValues(){
        return Stream.range(0, random.nextInt(maxNumValues)+1)
                .map(i -> {
                    double r;
                    do{
                        r = random.nextGaussian()*this.std + this.mean;
                    }while(r < start || r > end);
                    return (int)Math.round(r);
                }).toJavaList();
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
    private int mean;
    private double std;
}
