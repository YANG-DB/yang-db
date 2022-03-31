package com.yangdb.logging.mdc;

/*-
 * #%L
 * logging
 * %%
 * Copyright (C) 2016 - 2022 The YangDb Graph Database Project
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

public enum LogType implements MDCWriter{
    start,
    log,
    metric,
    success,
    failure;

    public static MDCWriter of(LogType logType) {
        return logType;
    }

    public static final String key = "logType";

    //region Constructors
    LogType() {
        this.mdcWriter = new MDCWriter.KeyValue<>(key, this.toString());
    }
    //endregion

    //region MDCWriter Implementation
    @Override
    public void write() {
        this.mdcWriter.write();
    }
    //endregion

    //region Fields
    private MDCWriter mdcWriter;
    //endregion
}
