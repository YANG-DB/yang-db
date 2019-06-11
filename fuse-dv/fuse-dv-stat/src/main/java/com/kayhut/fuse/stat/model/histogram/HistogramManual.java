package com.kayhut.fuse.stat.model.histogram;

/*-
 * #%L
 * fuse-dv-stat
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.kayhut.fuse.stat.model.enums.DataType;
import com.kayhut.fuse.stat.model.bucket.BucketRange;
import com.kayhut.fuse.stat.model.enums.HistogramType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 30-Apr-17.
 */
public class HistogramManual<T> extends Histogram {

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

    //endregion

    //region Fields
    private List<BucketRange<T>> buckets;
    //endregion

    //region Builder
    public static final class Builder {
        private List<BucketRange> buckets;
        private DataType dataType;

        private Builder() {
            this.buckets = new ArrayList<>();
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withBuckets(List<BucketRange> buckets) {
            this.buckets = buckets;
            return this;
        }

        public Builder withBucket(BucketRange bucket) {
            this.buckets.add(bucket);
            return this;
        }

        public Builder withDataType(DataType dataType) {
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
