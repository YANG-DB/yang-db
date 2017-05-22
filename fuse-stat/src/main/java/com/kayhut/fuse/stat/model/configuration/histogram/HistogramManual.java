package com.kayhut.fuse.stat.model.configuration.histogram;

import com.kayhut.fuse.stat.model.configuration.bucket.BucketRange;

import java.util.List;

/**
 * Created by benishue on 30-Apr-17.
 */
public class HistogramManual extends Histogram {

    //region Ctrs
    public HistogramManual() {
        super(HistogramType.manual);
    }
    //endregion

    //region Getters & Setters

    public List<BucketRange> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<BucketRange> buckets) {
        this.buckets = buckets;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    //endregion

    //region Fields
    private List<BucketRange> buckets;
    private String dataType;
    //endregion

    //region Builder
    public static final class HistogramManualBuilder {
        private List<BucketRange> buckets;
        private String dataType;

        private HistogramManualBuilder() {
        }

        public static HistogramManualBuilder aHistogramManual() {
            return new HistogramManualBuilder();
        }

        public HistogramManualBuilder withBuckets(List<BucketRange> buckets) {
            this.buckets = buckets;
            return this;
        }

        public HistogramManualBuilder withDataType(String dataType) {
            this.dataType = dataType;
            return this;
        }

        public HistogramManual build() {
            HistogramManual histogramManual = new HistogramManual();
            histogramManual.setBuckets(buckets);
            histogramManual.setDataType(dataType);
            return histogramManual;
        }
    }
    //endregion
}
