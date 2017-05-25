package com.kayhut.fuse.stat.model.histogram;

import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.enums.HistogramType;

import java.util.List;

/**
 * Created by benishue on 30-Apr-17.
 */
public class HistogramComposite <T> extends Histogram {

    //region Ctrs
    public HistogramComposite() {
        super(HistogramType.composite);
    }
    //endregion

    //region Getters & Setters

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public List<BucketRange<T>> getManualBuckets() {
        return manualBuckets;
    }

    public void setManualBuckets(List<BucketRange <T>> manualBuckets) {
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
    private DataType dataType;
    private List<BucketRange<T>> manualBuckets;
    public Histogram autoBuckets;
    //endregion

    //region Builder
    public static final class HistogramCompositeBuilder {
        public Histogram autoBuckets;
        private DataType dataType;
        private List<BucketRange> manualBuckets;

        private HistogramCompositeBuilder() {
        }

        public static HistogramCompositeBuilder aHistogramComposite() {
            return new HistogramCompositeBuilder();
        }

        public HistogramCompositeBuilder withDataType(DataType dataType) {
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
