package com.kayhut.fuse.stat.model.histogram;

import com.kayhut.fuse.stat.model.enums.HistogramType;

/**
 * Created by benishue on 30-Apr-17.
 */
public class HistogramString extends Histogram {

    //region Ctrs
    public HistogramString() {
        super(HistogramType.string);
    }
    //endregion

    //region Getters & Setters
    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getPrefixSize() {
        return prefixSize;
    }

    public void setPrefixSize(int prefixSize) {
        this.prefixSize = prefixSize;
    }

    public String getFirstCharCode() {
        return firstCharCode;
    }

    public void setFirstCharCode(String firstCharCode) {
        this.firstCharCode = firstCharCode;
    }

    public int getNumOfChars() {
        return numOfChars;
    }

    public void setNumOfChars(int numOfChars) {
        this.numOfChars = numOfChars;
    }
    //endregion

    //region Fields
    private int interval;
    private int prefixSize;
    private String firstCharCode;
    private int numOfChars;
    //endregion

    //region Builder
    public static final class Builder {
        private int interval;
        private int prefixSize;
        private String firstCharCode;
        private int numOfChars;

        private Builder() {
        }

        public static Builder aHistogramString() {
            return new Builder();
        }

        public Builder withInterval(int interval) {
            this.interval = interval;
            return this;
        }

        public Builder withPrefixSize(int prefixSize) {
            this.prefixSize = prefixSize;
            return this;
        }

        public Builder withFirstCharCode(String firstCharCode) {
            this.firstCharCode = firstCharCode;
            return this;
        }

        public Builder withNumOfChars(int numOfChars) {
            this.numOfChars = numOfChars;
            return this;
        }

        public HistogramString build() {
            HistogramString histogramString = new HistogramString();
            histogramString.setInterval(interval);
            histogramString.setPrefixSize(prefixSize);
            histogramString.setFirstCharCode(firstCharCode);
            histogramString.setNumOfChars(numOfChars);
            return histogramString;
        }
    }
    //endregion

}
