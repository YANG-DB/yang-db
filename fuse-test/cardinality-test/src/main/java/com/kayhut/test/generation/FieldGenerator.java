package com.kayhut.test.generation;

import java.util.List;

public interface FieldGenerator {
    List<Integer> generateValues();

    String getFieldName();
}
