package com.yangdb.fuse.unipop.controller.utils;

/*-
 * #%L
 * fuse-dv-unipop
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

import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;

/**
 * Created by lior.perry on 19/03/2017.
 */
public class CollectionUtil {
    public static <T> List<T> listFromObjectValue(Object value) {
        if (value == null) {
            return Collections.emptyList();
        } else if (Iterable.class.isAssignableFrom(value.getClass())) {
            return Stream.ofAll((Iterable)value).map(o -> (T)o).toJavaList();
        } else if (value.getClass().isArray()) {
            return Stream.of((T[])value).toJavaList();
        } else {
            return Stream.of((T)value).toJavaList();
        }
    }
}
