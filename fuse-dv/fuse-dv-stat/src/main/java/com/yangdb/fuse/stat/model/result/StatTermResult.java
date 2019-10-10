package com.yangdb.fuse.stat.model.result;

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

import com.yangdb.fuse.stat.model.enums.DataType;

/**
 * Created by benishue on 24/05/2017.
 */
public class StatTermResult <T> extends StatResultBase{

    //region Ctors
    public StatTermResult() {
        super();
    }

    public StatTermResult(String index,
                          String type,
                          String field,
                          String key,
                          DataType dataType,
                          T term,
                          long count,
                          long cardinality) {
        super(index, type, field, key, dataType, count, cardinality);
        this.term = term;
    }

    //endregion

    //region Getter & Setters
    public T getTerm() {
        return term;
    }

    public void setTerm(T term) {
        this.term = term;
    }
    //endregion

    //region Fields
    private T term;
    //endregion
}
