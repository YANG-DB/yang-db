package org.unipop.schema.property.type;

/*-
 * #%L
 * DateType.java - unipop-core - kayhut - 2,016
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

import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.unipop.process.predicate.Date;

import java.util.function.BiPredicate;

/**
 * Created by sbarzilay on 8/19/16.
 */
public class DateType implements PropertyType {
    @Override
    public String getType() {
        return "DATE";
    }

    @Override
    public <V> P<V> translate(P<V> predicate) {
        BiPredicate<V, V> biPredicate = predicate.getBiPredicate();
        if (biPredicate instanceof Compare){
            String predicateString = biPredicate.toString();
            V value = predicate.getValue();
            switch (predicateString){
                case "eq":
                    return Date.eq(value);
                case "neq":
                    return Date.neq(value);
                case "lt":
                    return Date.lt(value);
                case "gt":
                    return Date.gt(value);
                case "lte":
                    return Date.lte(value);
                case "gte":
                    return Date.gte(value);
                default:
                    throw new IllegalArgumentException("cant convert '" + predicateString +"' to DatePredicate");
            }
        } else
            throw new IllegalArgumentException("cant convert '" + biPredicate.toString() +"' to DatePredicate");
    }
}
