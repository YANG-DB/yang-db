package com.yangdb.fuse.stat.model.histogram;

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

import com.yangdb.fuse.stat.model.enums.DataType;
import com.yangdb.fuse.stat.model.enums.HistogramType;

/**
 * Created by benishue on 30-Apr-17.
 */
public class HistogramNumeric extends Histogram {

    //region Ctrs
    public HistogramNumeric() {
        super(HistogramType.numeric);
    }
    //endregion

    //region Getters & Setters
    public Number getMin() {
        return min;
    }

    public void setMin(Number min) {
        this.min = min;
    }

    public Number getMax() {
        return max;
    }

    public void setMax(Number max) {
        this.max = max;
    }

    public int getNumOfBins() {
        return numOfBins;
    }

    public void setNumOfBins(int numOfBins) {
        this.numOfBins = numOfBins;
    }
    //endregion

    //region Fields
    private Number min;
    private Number max;
    private int numOfBins;
    //endregion

    //region Builder
    public static final class Builder {
        private Number min;
        private Number max;
        private int numOfBins;
        private DataType dataType;

        private Builder() {
            super();
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withMin(Number min) {
            this.min = min;
            return this;
        }

        public Builder withMax(Number max) {
            this.max = max;
            return this;
        }

        public Builder withNumOfBins(int numOfBins) {
            this.numOfBins = numOfBins;
            return this;
        }

        public Builder withDataType(DataType dataType) {
            this.dataType = dataType;
            return this;
        }

        public HistogramNumeric build() {
            HistogramNumeric histogramNumeric = new HistogramNumeric();
            histogramNumeric.setMin(min);
            histogramNumeric.setMax(max);
            histogramNumeric.setDataType(dataType);
            histogramNumeric.setNumOfBins(numOfBins);
            return histogramNumeric;
        }
    }
    //endregion

}
