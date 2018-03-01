package com.kayhut.test.tests;

import com.clearspring.analytics.stream.cardinality.CardinalityMergeException;

public interface Test {
    TestResults run() throws CardinalityMergeException;
}
