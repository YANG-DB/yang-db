package com.kayhut.fuse.stat.model.configuration.histogram;

import com.kayhut.fuse.stat.model.configuration.bucket.BucketRange;

import java.util.List;

/**
 * Created by benishue on 30-Apr-17.
 */
public class HistogramComposite extends Histogram {

    //region Ctrs
    public HistogramComposite() {
        super(HistogramType.composite);
    }
    //endregion

    //region Getters & Setters

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public List<BucketRange> getManualBuckets() {
        return manualBuckets;
    }

    public void setManualBuckets(List<BucketRange> manualBuckets) {
        this.manualBuckets = manualBuckets;
    }

    public Histogram getAutoBuckets() {
        return autoBuckets;
    }

    public void setAutoBuckets(Histogram autoBuckets) {
        this.autoBuckets = autoBuckets;
    }

    //endregion

    //region Fields
    private String dataType;
    private List<BucketRange> manualBuckets;
    public Histogram autoBuckets;
    //endregion

    //region Builder
    public static final class HistogramCompositeBuilder {
        public Histogram autoBuckets;
        private String dataType;
        private List<BucketRange> manualBuckets;

        private HistogramCompositeBuilder() {
        }

        public static HistogramCompositeBuilder aHistogramComposite() {
            return new HistogramCompositeBuilder();
        }

        public HistogramCompositeBuilder withDataType(String dataType) {
            this.dataType = dataType;
            return this;
        }

        public HistogramCompositeBuilder withManualBuckets(List<BucketRange> manualBuckets) {
            this.manualBuckets = manualBuckets;
            return this;
        }

        public HistogramCompositeBuilder withAutoBuckets(Histogram autoBuckets) {
            this.autoBuckets = autoBuckets;
            return this;
        }

        public HistogramComposite build() {
            HistogramComposite histogramComposite = new HistogramComposite();
            histogramComposite.setDataType(dataType);
            histogramComposite.setManualBuckets(manualBuckets);
            histogramComposite.setAutoBuckets(autoBuckets);
            return histogramComposite;
        }
    }
    //endregion

}
