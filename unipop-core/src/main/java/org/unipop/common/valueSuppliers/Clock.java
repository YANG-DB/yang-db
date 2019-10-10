package org.unipop.common.valueSuppliers;

/*-
 *
 * Clock.java - unipop-core - yangdb - 2,016
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
 * Created by Roman on 8/21/2018.
 */
public interface Clock {
    long getTime();

    class System implements Clock {
        public static System instance = new System();

        //region Clock Implementation
        @Override
        public long getTime() {
            return java.lang.System.currentTimeMillis();
        }
        //endregion
    }

    class Manual implements Clock {
        //region Constructors
        public Manual() {
            this(0L);
        }

        public Manual(long time) {
            this.time = time;
        }
        //endregion

        //region Clock Implementation
        @Override
        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }
        //endregion

        //region Fields
        private long time;
        //endregion
    }
}
