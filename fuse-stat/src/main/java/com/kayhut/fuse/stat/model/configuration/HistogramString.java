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

    //region Fields
    private String interval;
    private String prefixSize;
    //endregion

    //region Builder
    public static final class HistogramStringBuilder {
        private String interval;
        private String prefixSize;

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

        public HistogramString build() {
            HistogramString histogramString = new HistogramString();
            histogramString.setInterval(interval);
            histogramString.setPrefixSize(prefixSize);
            return histogramString;
        }
    }
    //endregion


}
