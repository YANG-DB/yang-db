package com.kayhut.fuse.stat.model.result;

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

/**
 * Created by benishue on 03-May-17.
 */
public class StatRangeResult<T> extends StatResultBase {

    //region Ctors
    public StatRangeResult() {
        super();
    }

    public StatRangeResult(String index, String type, String field, String key, DataType dataType, T lowerBound, T upperBound, long count, long cardinality) {
        super(index, type, field, key, dataType, count, cardinality);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    //endregion

    //region Getter & Setters
    public T getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(T lowerBound) {
        this.lowerBound = lowerBound;
    }

    public T getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(T upperBound) {
        this.upperBound = upperBound;
    }
    //endregion

    //region Fields
    private T lowerBound;
    private T upperBound;
    //endregion
}
