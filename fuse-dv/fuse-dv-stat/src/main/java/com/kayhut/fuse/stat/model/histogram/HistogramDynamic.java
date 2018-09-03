package com.kayhut.fuse.stat.model.histogram;

import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.enums.HistogramType;

/**
 * Created by benishue on 30-Apr-17.
 */
public class HistogramDynamic extends Histogram {

    //region Ctrs
    public HistogramDynamic() {
        super(HistogramType.dynamic);
    }
    //endregion

    //region Getters & Setters
    public int getNumOfBins() {
        return numOfBins;
    }

    public void setNumOfBins(int numOfBins) {
        this.numOfBins = numOfBins;
    }
    //endregion

    //region Fields
    private int numOfBins;
    //endregion

    //region Builder
    public static final class Builder {
        private int numOfBins;
        private DataType dataType;

        private Builder() {
            super();
        }

        public static Builder get() {
            return new Builder();
        }



        public Builder withNumOfBins(int numOfBins) {
            this.numOfBins = numOfBins;
            return this;
        }

        public Builder withDataType(DataType dataType) {
            this.dataType = dataType;
            return this;
        }

        public HistogramDynamic build() {
            HistogramDynamic histogramDynamic = new HistogramDynamic();
            histogramDynamic.setDataType(dataType);
            histogramDynamic.setNumOfBins(numOfBins);
            return histogramDynamic;
        }
    }
    //endregion

}
