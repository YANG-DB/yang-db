package com.kayhut.fuse.stat.model.configuration;

/**
 * Created by benishue on 30-Apr-17.
 */
public class HistogramNumeric extends Histogram {

    public HistogramNumeric() {
        super(HistogramType.numeric);
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getNumOfBins() {
        return numOfBins;
    }

    public void setNumOfBins(String numOfBins) {
        this.numOfBins = numOfBins;
    }

    //region Fields
    private String min;
    private String max;
    private String numOfBins;
    //endregion

    //region Builder
    public static final class HistogramNumericBuilder {
        private String min;
        private String max;
        private String numOfBins;

        private HistogramNumericBuilder() {
            super();
        }

        public static HistogramNumericBuilder aHistogramNumeric() {
            return new HistogramNumericBuilder();
        }

        public HistogramNumericBuilder withMin(String min) {
            this.min = min;
            return this;
        }

        public HistogramNumericBuilder withMax(String max) {
            this.max = max;
            return this;
        }

        public HistogramNumericBuilder withNumOfBins(String interval) {
            this.numOfBins = interval;
            return this;
        }

        public HistogramNumeric build() {
            HistogramNumeric histogramNumeric = new HistogramNumeric();
            histogramNumeric.setMin(min);
            histogramNumeric.setMax(max);
            histogramNumeric.setNumOfBins(numOfBins);
            return histogramNumeric;
        }
    }
    //endregion


}
