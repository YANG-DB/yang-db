package com.kayhut.fuse.model.query.aggregation;

/*-
 * #%L
 * AggBase.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import com.kayhut.fuse.model.query.EBase;

/**
 * Created by lior.perry on 22/02/2017.
 */
public class AggBase extends EBase {
    //region Properties
    public String[] getPer() {
        return this.per;
    }

    public void setPer(String[] value) {
        this.per = value;
    }

    public int getB() {
        return this.b;
    }

    public void setB(int value) {
        this.b = value;
    }
    //endregion

    //region Fields
    private String[] per;
    private int b;
    //endregion
}
