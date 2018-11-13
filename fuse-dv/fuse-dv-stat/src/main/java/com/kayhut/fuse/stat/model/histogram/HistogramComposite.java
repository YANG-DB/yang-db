package com.kayhut.fuse.stat.model.histogram;

/*-
 * #%L
 * fuse-dv-stat
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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
public class HistogramComposite <T> extends Histogram {

    //region Ctrs
    public HistogramComposite() {
        super(HistogramType.composite);
    }
    //endregion

    //region Getters & Setters

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
    private List<BucketRange<T>> manualBuckets;
    public Histogram autoBuckets;
    //endregion

    //region Builder
    public static final class Builder {
        public Histogram autoBuckets;
        private DataType dataType;
        private List<BucketRange> manualBuckets;

        private Builder() {
            this.manualBuckets = new ArrayList<>();
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withDataType(DataType dataType) {
            this.dataType = dataType;
            return this;
        }

        public Builder withManualBuckets(List<BucketRange> manualBuckets) {
            this.manualBuckets = manualBuckets;
            return this;
        }

        public Builder withBucket(BucketRange bucket) {
            this.manualBuckets.add(bucket);
            return this;
        }

        public Builder withAutoBuckets(Histogram autoBuckets) {
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
