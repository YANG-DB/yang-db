package com.yangdb.fuse.asg.util;

/*-
 *
 * fuse-asg
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

import com.yangdb.fuse.model.ontology.Property;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;

public class OntologyPropertyTypeFactory {

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    //region Constructor
    public OntologyPropertyTypeFactory() {
        this.map = new HashMap<>();
        this.map.put("string", new StringFunction());
        this.map.put("int", new LongFunction());
        this.map.put("float", new DoubleFunction());
        this.map.put("double", new DoubleFunction());
        this.map.put("date", new DateFunction());
    }
    //endregion

    //region Public Methods
    public Object supply(Property prop, Object exp) {
        if (exp != null) {
            Function<Object, Object> tranformFunction = this.map.get(prop.getType());
            if (tranformFunction != null) {
                return tranformFunction.apply(exp);
            }
        }

        return exp;
    }
    //endregion

    //region Fields
    private Map<String, Function<Object, Object>> map;
    //endregion

    //Functions
    private class StringFunction implements Function<Object, Object> {

        @Override
        public Object apply(Object o) {
            return o;
        }
    }

    private class LongFunction implements Function<Object, Object> {

        @Override
        public Object apply(Object o) {
            if (o instanceof String) {
                return Long.parseLong((String) o);
            }

            if (Number.class.isAssignableFrom(o.getClass())) {
                return ((Number) o).longValue();
            }

            return o;
        }
    }

    private class DoubleFunction implements Function<Object, Object> {

        @Override
        public Object apply(Object o) {
            if (o instanceof String) {
                return Double.parseDouble((String) o);
            }

            if (Number.class.isAssignableFrom(o.getClass())) {
                return ((Number) o).doubleValue();
            }

            return o;
        }
    }

    private class DateFunction implements Function<Object, Object> {

        @Override
        public Object apply(Object o) {
            if (o instanceof Date)
                return o;

            if (Number.class.isAssignableFrom(o.getClass()))
                return new Date(((Number) o).longValue());

            if (o instanceof String) {
                try {
                    return sdf.parse(o.toString());
                } catch (Exception err) {
                    //faild parsing date
                }
            }
            return o;
        }
    }
    //endregion
}
