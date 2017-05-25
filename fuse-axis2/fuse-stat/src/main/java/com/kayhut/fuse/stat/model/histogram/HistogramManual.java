package com.kayhut.fuse.stat.model.histogram;

import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.enums.HistogramType;

import java.util.List;

/**
 * Created by benishue on 30-Apr-17.
 */
public class HistogramManual <T> extends Histogram {

    //region Ctrs
    public HistogramManual() {
        super(HistogramType.manual);
    }
    //endregion

    //region Getters & Setters

    public List<BucketRange<T>> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<BucketRange<T>> buckets) {
        this.buckets = buckets;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }
    //endregion

    //region Fields
    private List<BucketRange<T>> buckets;
    private DataType dataType;
    //endregion

    //region Builder
    public static final class HistogramManualBuilder {
        private List<BucketRange> buckets;
        private DataType dataType;

        private HistogramManualBuilder() {
        }

        public static HistogramManualBuilder aHistogramManual() {
            return new HistogramManualBuilder();
        }

        public HistogramManualBuilder withBuckets(List<BucketRange> buckets) {
            this.buckets = buckets;
            return this;
        }

        public HistogramManualBuilder withDataType(DataType dataType) {
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
