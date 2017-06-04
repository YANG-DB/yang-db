package com.kayhut.fuse.stat.model.histogram;

import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.enums.HistogramType;

/**
 * Created by benishue on 30-Apr-17.
 */
public class HistogramNumeric extends Histogram {

    //region Ctrs
    public HistogramNumeric() {
        super(HistogramType.numeric);
    }
    //endregion

    //region Getters & Setters
    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public int getNumOfBins() {
        return numOfBins;
    }

    public void setNumOfBins(int numOfBins) {
        this.numOfBins = numOfBins;
    }
    //endregion

    //region Fields
    private double min;
    private double max;
    private int numOfBins;
    //endregion

    //region Builder
    public static final class Builder {
        private double min;
        private double max;
        private int numOfBins;

        private Builder() {
            super();
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withMin(double min) {
            this.min = min;
            return this;
        }

        public Builder withMax(double max) {
            this.max = max;
            return this;
        }

        public Builder withNumOfBins(int numOfBins) {
            this.numOfBins = numOfBins;
            return this;
        }

        public HistogramNumeric build() {
            HistogramNumeric histogramNumeric = new HistogramNumeric();
            histogramNumeric.setMin(min);
            histogramNumeric.setMax(max);
            histogramNumeric.setDataType(DataType.numeric);
            histogramNumeric.setNumOfBins(numOfBins);
            return histogramNumeric;
        }
    }
    //endregion

}
