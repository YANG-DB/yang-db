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

public class MethodName {
    //region Static
    public static Value of(String methodName) {
        return new Value(methodName);
    }
    //endregion

    public static class Value extends MDCWriter.KeyValue<String> {
        public static Value empty = new Value(null);

        //region Constructors
        public Value(String methodName) {
            super(MethodName.key, methodName);
        }
        //endregion

        //region Properties
        public String getMethodName() {
            return this.value;
        }
        //endregion

        //region Override Methods
        @Override
        public String toString() {
            return this.value;
        }
        //endregion
    }

    public static final String key = "methodName";
}
