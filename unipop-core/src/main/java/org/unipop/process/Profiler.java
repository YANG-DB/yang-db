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

import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Profiler {
    String PROFILER = "profiler";

    Profiler add(String name, Impl.Measurement measurement);

    /**
     * get measurement
     * @param key
     * @return
     */
    Impl.Measurement get(String key);

    Impl.Measurement getOrCreate(String label);

    class Noop implements Profiler {
        public static Noop instance = new Noop();
        public static Impl.Measurement measurement = new Impl.Measurement("NOOP",-1);

        @Override
        public Profiler add(String name, Impl.Measurement measurement) {
            return this;
        }

        @Override
        public Impl.Measurement get(String key) {
            return measurement;
        }

        @Override
        public Impl.Measurement getOrCreate(String key) {
            return measurement;
        }

    }

    class Impl implements Profiler {
        //region Constructors
        public Impl() {
            this.measurements = new HashMap<>(200);
        }
        //endregion

        //region Public Methods
        public Profiler add(String name, Measurement measurement) {
            this.measurements.put(name,measurement);
            return this;
        }

        public Measurement get(String key) {
            return this.measurements.get(key);
        }

        @Override
        public Measurement getOrCreate(String key) {
            return this.measurements.computeIfAbsent(key,s->new Measurement(key,0));
        }

        @Override
        public String toString() {
            Map<String, Tuple2<Integer, Long>> sums = Stream.ofAll(this.measurements.values()).groupBy(Measurement::getName)
                    .toJavaMap(grouping -> new Tuple2<>(grouping._1(), new Tuple2<>(grouping._2().size(), grouping._2().map(Measurement::getElapsed).sum().longValue())));

            StringBuilder builder = new StringBuilder();
            sums.forEach((key, value) -> builder.append(key).append(" = { count: ").append(value._1())
                    .append(", sum: ").append(value._2()).append(" }\n"));
            return builder.toString();
        }
        //endregion

        //region Properties
        public Measurement getMeasurements(String name) {
            return measurements.get(name);
        }
        //endregion

        //region Fields
        private Map<String,Measurement> measurements;
        //endregion

        public static class Measurement {
            //region Constructors
            public Measurement(String name, long elapsed) {
                this.name = name;
                this.elapsed = elapsed;
            }

                //endregion
            public Measurement inc(long value) {
                this.elapsed+=value;
                return this;
            }

            //region Properties
            public String getName() {
                return name;
            }

            public long getElapsed() {
                return elapsed;
            }
            //endregion

            //region Fields
            private String name;
            private long elapsed;
            //endregion
        }
    }
}
