package com.kayhut.test.tests;

import com.kayhut.test.generation.FieldGenerator;
import com.kayhut.test.generation.GaussianFieldGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class MultiModalGaussianIntersectionBase implements Test {

    protected int numFields = 3;
    protected Random random;
    protected int histogramLower = 1;
    protected int histogramUpper=1101;
    private double overlapRatio;

    protected MultiModalGaussianIntersectionBase(Random random, double overlapRatio) {
        this.random = random;
        this.overlapRatio = overlapRatio;
    }

    protected  List<FieldGenerator> getFieldGenerators( ){
        List<FieldGenerator> fieldGeneratorList = new ArrayList<>();
        int width = 500 / (numFields-1);
        for (int i = 0; i < numFields - 1; i++) {
            fieldGeneratorList.add(new GaussianFieldGenerator("field"+(i+1),1,
                    random,
                    width*i + 1,
                    (int)(width*(i+1 + overlapRatio)),(int)(width*(1+overlapRatio)/2) + width*i,width*(1+overlapRatio)/4));
        }

        fieldGeneratorList.add(new GaussianFieldGenerator("field" + numFields,1,random,1000,1100,1050,30));
        return fieldGeneratorList;
    }
}
