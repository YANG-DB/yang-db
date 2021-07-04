package org.unipop.process.predicate;

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

import org.apache.tinkerpop.gremlin.process.traversal.P;

/**
 * distinct filter is a composite terms filter aggregation - it should return distinct elements which may appear multiple times
 */
public class DistinctFilterP<V> extends P<V> {

    private DistinctFilterP() {
        super(null, null);
    }

    public static <V> DistinctFilterP<V> distinct() {
        return new DistinctFilterP<V>();
    }

    @Override
    public String toString() {
        return "DistinctFilterP{}";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof P &&
                ((P) other).getClass().equals(this.getClass());

    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public static boolean hasDistinct(org.apache.tinkerpop.gremlin.process.traversal.step.filter.HasStep<?> step) {
        return step.getHasContainers().stream().anyMatch(c->DistinctFilterP.class.isAssignableFrom(c.getPredicate().getClass()));
    }

//endregion
}
