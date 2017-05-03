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

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    //region Fields
    private String min;
    private String max;
    private String interval;
    //endregion

    //region Builder
    public static final class HistogramNumericBuilder {
        private String min;
        private String max;
        private String interval;

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

        public HistogramNumericBuilder withInterval(String interval) {
            this.interval = interval;
            return this;
        }

        public HistogramNumeric build() {
            HistogramNumeric histogramNumeric = new HistogramNumeric();
            histogramNumeric.setMin(min);
            histogramNumeric.setMax(max);
            histogramNumeric.setInterval(interval);
            return histogramNumeric;
        }
    }
    //endregion


}
