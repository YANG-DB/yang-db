package com.kayhut.fuse.model.query.aggregation;

/*-
 * #%L
 * AggM3.java - fuse-model - kayhut - 2,016
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

/**
 * Created by lior.perry on 19/02/2017.
 */
public class AggM3 extends AggMBase {
    //region Properties
    public String[] getETag() {
        return this.eTag;
    }

    public void setETag(String[] value) {
        this.eTag = value;
    }

    public AggLOp getAggOp() {
        return this.aggOp;
    }

    public void setAggOp(AggLOp aggOp) {
        this.aggOp = aggOp;
    }

    public int getPType() {
        return this.pType;
    }

    public void setpType(int value) {
        this.pType = value;
    }
    //endregion

    //region Fields
    private String[] eTag;
    private AggLOp aggOp;
    private int pType;
    //endregion
}
