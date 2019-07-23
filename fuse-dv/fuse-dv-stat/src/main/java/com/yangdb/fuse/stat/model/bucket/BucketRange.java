package com.yangdb.fuse.stat.model.bucket;

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

/**
 * Created by benishue on 30-Apr-17.
 */
public class BucketRange<T> extends Bucket {

    //region Ctrs
    public BucketRange() {
        super();
    }

    public BucketRange(T start, T end) {
        super();
        this.start = start;
        this.end = end;
    }
    //endregion

    //region Getters & Setters
    public T getStart() {
        return start;
    }

    public void setStart(T start) {
        this.start = start;
    }

    public T getEnd() {
        return end;
    }

    public void setEnd(T end) {
        this.end = end;
    }
    //endregion

    //region Fields
    private T start;
    private T end;
    //endregion

}
