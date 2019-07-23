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
public class HistogramDynamic extends Histogram {

    //region Ctrs
    public HistogramDynamic() {
        super(HistogramType.dynamic);
    }
    //endregion

    //region Getters & Setters
    public int getNumOfBins() {
        return numOfBins;
    }

    public void setNumOfBins(int numOfBins) {
        this.numOfBins = numOfBins;
    }
    //endregion

    //region Fields
    private int numOfBins;
    //endregion

    //region Builder
    public static final class Builder {
        private int numOfBins;
        private DataType dataType;

        private Builder() {
            super();
        }

        public static Builder get() {
            return new Builder();
        }



        public Builder withNumOfBins(int numOfBins) {
            this.numOfBins = numOfBins;
            return this;
        }

        public Builder withDataType(DataType dataType) {
            this.dataType = dataType;
            return this;
        }

        public HistogramDynamic build() {
            HistogramDynamic histogramDynamic = new HistogramDynamic();
            histogramDynamic.setDataType(dataType);
            histogramDynamic.setNumOfBins(numOfBins);
            return histogramDynamic;
        }
    }
    //endregion

}
