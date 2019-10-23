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

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Optional;

/**
 * Created by Roman on 9/23/2017.
 */
public class ElementUtil {
    public static <V> Optional<V> value(Element element, String key){
        if (key.equals(T.id.getAccessor())) {
            return Optional.of((V)element.id());
        }

        if (element.keys().contains(key)) {
            return Optional.of(element.value(key));
        } else {
            return Optional.empty();
        }
    }
}
