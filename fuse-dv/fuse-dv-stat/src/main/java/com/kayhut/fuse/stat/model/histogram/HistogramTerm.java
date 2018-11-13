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
import com.kayhut.fuse.stat.model.bucket.BucketTerm;
import com.kayhut.fuse.stat.model.enums.HistogramType;

import java.util.ArrayList;
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
    //endregion

    //region Fields
    private List<BucketTerm> buckets;
    //endregion

    //region Builder
    public static final class Builder {
        private List<BucketTerm> buckets;
        private DataType dataType;

        private Builder() {
            this.buckets = new ArrayList<>();
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withBuckets(List<BucketTerm> buckets) {
            this.buckets = buckets;
            return this;
        }

        public Builder withTerms(List<String> terms) {
            terms.forEach(term -> this.buckets.add(new BucketTerm<>(term)));
            return this;
        }

        public Builder withTerm(String term) {
            this.buckets.add(new BucketTerm<>(term));
            return this;
        }

        public Builder withBucket(BucketTerm bucket) {
            this.buckets.add(bucket);
            return this;
        }

        public Builder withDataType(DataType dataType) {
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
