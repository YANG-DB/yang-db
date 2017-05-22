package com.kayhut.fuse.stat.model.configuration.histogram;

import com.kayhut.fuse.stat.model.configuration.bucket.BucketTerm;

import java.util.List;

/**
 * Created by benishue on 22-May-17.
 */
public class HistogramTerm extends Histogram {

    //region Ctrs
    public HistogramTerm() {
        super(HistogramType.term);
    }
    //endregion

    //region Getters & Setters
    public List<BucketTerm> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<BucketTerm> buckets) {
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
    private List<BucketTerm> buckets;
    private String dataType;
    //endregion

    //region Builder
    public static final class HistogramTermBuilder {
        private List<BucketTerm> buckets;
        private String dataType;

        private HistogramTermBuilder() {
        }

        public static HistogramTermBuilder aHistogramTerm() {
            return new HistogramTermBuilder();
        }

        public HistogramTermBuilder withBuckets(List<BucketTerm> buckets) {
            this.buckets = buckets;
            return this;
        }

        public HistogramTermBuilder withDataType(String dataType) {
            this.dataType = dataType;
            return this;
        }

        public HistogramTerm build() {
            HistogramTerm histogramTerm = new HistogramTerm();
            histogramTerm.setBuckets(buckets);
            histogramTerm.setDataType(dataType);
            return histogramTerm;
        }
    }
    //endregion

}
