package com.yangdb.fuse.stat.model.histogram;

/*-
 *
 * fuse-dv-stat
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.stat.model.enums.HistogramType;

/**
 * Created by benishue on 30-Apr-17.
 */
public class HistogramString extends Histogram {

    //region Ctrs
    public HistogramString() {
        super(HistogramType.string);
    }
    //endregion

    //region Getters & Setters
    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getPrefixSize() {
        return prefixSize;
    }

    public void setPrefixSize(int prefixSize) {
        this.prefixSize = prefixSize;
    }

    public String getFirstCharCode() {
        return firstCharCode;
    }

    public void setFirstCharCode(String firstCharCode) {
        this.firstCharCode = firstCharCode;
    }

    public int getNumOfChars() {
        return numOfChars;
    }

    public void setNumOfChars(int numOfChars) {
        this.numOfChars = numOfChars;
    }
    //endregion

    //region Fields
    private int interval;
    private int prefixSize;
    private String firstCharCode;
    private int numOfChars;
    //endregion

    //region Builder
    public static final class Builder {
        private int interval;
        private int prefixSize;
        private String firstCharCode;
        private int numOfChars;

        private Builder() {
        }

        public static Builder get() {
            return new Builder();
        }

        public Builder withInterval(int interval) {
            this.interval = interval;
            return this;
        }

        public Builder withPrefixSize(int prefixSize) {
            this.prefixSize = prefixSize;
            return this;
        }

        public Builder withFirstCharCode(String firstCharCode) {
            this.firstCharCode = firstCharCode;
            return this;
        }

        public Builder withNumOfChars(int numOfChars) {
            this.numOfChars = numOfChars;
            return this;
        }

        public HistogramString build() {
            HistogramString histogramString = new HistogramString();
            histogramString.setInterval(interval);
            histogramString.setPrefixSize(prefixSize);
            histogramString.setFirstCharCode(firstCharCode);
            histogramString.setNumOfChars(numOfChars);
            return histogramString;
        }
    }
    //endregion

}
