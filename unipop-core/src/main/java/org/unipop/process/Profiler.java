package org.unipop.process;

/*-
 * #%L
 * unipop-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

/*-
 *
 * Profiler.java - unipop-core - yangdb - 2,016
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

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.util.ImmutableMetrics;
import org.apache.tinkerpop.gremlin.process.traversal.util.Metrics;
import org.apache.tinkerpop.gremlin.process.traversal.util.MutableMetrics;

public interface Profiler {
    String PROFILER = "profiler";

    Profiler add(String name);

    /**
     * get measurement
     * @return
     */
    MutableMetrics get();

    class Noop implements Profiler {
        public static Noop instance = new Noop();
        public static MutableMetrics measurement = new MutableMetrics(new ImmutableMetrics() {});


        @Override
        public Profiler add(String name) {
            return instance;
        }

        @Override
        public MutableMetrics get() {
            return measurement;
        }
    }

    class Impl implements Profiler {
        //region Constructors
        public Impl() {
            this.measurements = new MutableMetrics() {  };
        }
        //endregion

        //region Public Methods
        public Profiler add(String name) {
            this.measurements = new MutableMetrics(name,name);
            return this;
        }

        //endregion
        @Override
        public MutableMetrics get() {
            return measurements;
        }

        @Override
        public String toString() {
            return StringUtils.join(measurements.getCounts());
        }

        //region Fields
        private MutableMetrics measurements;
        //endregion

    }
}
