package com.kayhut.fuse.model.query.properties;

/*-
 * #%L
 * ScoreEProp.java - fuse-model - kayhut - 2,016
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.projection.Projection;

/**
 * Eprop with a boost to rank the query results according to the desired boost
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ScoreEProp extends EProp implements RankingProp {
    //region Constructors
    public ScoreEProp(EProp eProp, long boost) {
        this(eProp.geteNum(),eProp.getpType(),eProp.getCon(),boost);
    }

    public ScoreEProp(int eNum, String pType, Constraint con, long boost) {
        super(eNum, pType, con);
        this.boost = boost;
    }

    public ScoreEProp(int eNum, String pType, Projection proj, long boost) {
        super(eNum, pType, proj);
        this.boost = boost;
    }
    //endregion
    public long getBoost() {
        return boost;
    }
    //region Properties

    //region Fields
    private long boost;
    //endregion

}
