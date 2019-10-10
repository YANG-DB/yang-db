package com.yangdb.fuse.model.transport;

/*-
 *
 * PlanTraceOptions.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

/**
 * Created by Roman on 12/23/2017.
 */
public class PlanTraceOptions {
    //region Level
    public enum Level {
        none,
        info,
        debug,
        trace,
        verbose
    }
    //endregion

    //region Static Methods
    public static PlanTraceOptions of(Level level) {
        PlanTraceOptions planTraceOptions = new PlanTraceOptions();
        planTraceOptions.setLevel(level);
        return planTraceOptions;
    }
    //endregion

    //region Constructors
    public PlanTraceOptions() {

    }
    //endregion

    //region Properties
    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }
    //endregion

    //region Fields
    private Level level;
    //endregion
}
