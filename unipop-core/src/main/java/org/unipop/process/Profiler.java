package org.unipop.process;

/*-
 * #%L
 * Profiler.java - unipop-core - kayhut - 2,016
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

import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Profiler {
    Profiler add(String name, long elapsed);

    class Noop implements Profiler {
        public static Noop instance = new Noop();

        @Override
        public Profiler add(String name, long elapsed) {
            return this;
        }
    }

    class Impl implements Profiler {
        //region Constructors
        public Impl() {
            this.measurements = new ArrayList<>(200);
        }
        //endregion

        //region Public Methods
        public Profiler add(String name, long elapsed) {
            this.measurements.add(new Measurement(name, elapsed));
            return this;
        }

        @Override
        public String toString() {
            Map<String, Tuple2<Integer, Long>> sums = Stream.ofAll(this.measurements).groupBy(Measurement::getName)
                    .toJavaMap(grouping -> new Tuple2<>(grouping._1(), new Tuple2<>(grouping._2().size(), grouping._2().map(Measurement::getElapsed).sum().longValue())));

            StringBuilder builder = new StringBuilder();
            sums.forEach((key, value) -> builder.append(key).append(" = { count: ").append(value._1())
                    .append(", sum: ").append(value._2()).append(" }\n"));
            return builder.toString();
        }
        //endregion

        //region Properties
        public List<Measurement> getMeasurements() {
            return measurements;
        }
        //endregion

        //region Fields
        private List<Measurement> measurements;
        //endregion

        public static class Measurement {
            //region Constructors
            public Measurement(String name, long elapsed) {
                this.name = name;
                this.elapsed = elapsed;
            }
            //endregion

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
