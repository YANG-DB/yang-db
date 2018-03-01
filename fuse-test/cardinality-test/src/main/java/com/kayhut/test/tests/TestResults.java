package com.kayhut.test.tests;

import java.util.List;

public class TestResults {
    public List<Double> getErrorRatios() {
        return errorRatios;
    }

    public void setErrorRatios(List<Double> errorRatios) {
        this.errorRatios = errorRatios;
    }

    private List<Double> errorRatios;
}
