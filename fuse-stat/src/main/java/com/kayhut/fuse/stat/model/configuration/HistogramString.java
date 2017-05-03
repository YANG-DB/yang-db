package com.kayhut.fuse.stat.model.configuration;

/**
 * Created by benishue on 30-Apr-17.
 */
public class HistogramString extends Histogram {

    public HistogramString() {
        super(HistogramType.string);
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getPrefixSize() {
        return prefixSize;
    }

    public void setPrefixSize(String prefixSize) {
        this.prefixSize = prefixSize;
    }

    public String getFirstCharCode() {
        return firstCharCode;
    }

    public void setFirstCharCode(String firstCharCode) {
        this.firstCharCode = firstCharCode;
    }

    public String getNumOfChars() {
        return numOfChars;
    }

    public void setNumOfChars(String numOfChars) {
        this.numOfChars = numOfChars;
    }

    //region Fields
    private String interval;
    private String prefixSize;
    private String firstCharCode;
    private String numOfChars;
    //endregion

    //region Builder
    public static final class HistogramStringBuilder {
        private String interval;
        private String prefixSize;
        private String firstCharCode;
        private String numOfChars;

        private HistogramStringBuilder() {
        }

        public static HistogramStringBuilder aHistogramString() {
            return new HistogramStringBuilder();
        }

        public HistogramStringBuilder withInterval(String interval) {
            this.interval = interval;
            return this;
        }

        public HistogramStringBuilder withPrefixSize(String prefixSize) {
            this.prefixSize = prefixSize;
            return this;
        }

        public HistogramStringBuilder withFirstCharCode(String firstCharCode) {
            this.firstCharCode = firstCharCode;
            return this;
        }

        public HistogramStringBuilder withNumOfChars(String numOfChars) {
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
