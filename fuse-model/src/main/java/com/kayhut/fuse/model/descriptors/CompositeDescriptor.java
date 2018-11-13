package com.kayhut.fuse.model.descriptors;

/*-
 * #%L
 * CompositeDescriptor.java - fuse-model - kayhut - 2,016
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

import javaslang.collection.Stream;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by roman.margolis on 30/11/2017.
 */
public class CompositeDescriptor<Q> implements Descriptor<Q>{
    //region Constructors
    public CompositeDescriptor(Map<Class<?>, Descriptor<? extends Q>> descriptors, Descriptor<? extends Q> defaultDescriptor) {
        this.descriptors = new HashMap<>(descriptors);
        this.defaultDescriptor = defaultDescriptor;
    }
    //endregion

    //region Descriptor Implementation
    @Override
    public String describe(Q item) {
        Optional<Descriptor<? extends Q>> descriptor = Stream.ofAll(this.descriptors.entrySet())
                .filter(entry -> entry.getKey().isAssignableFrom(item.getClass()))
                .sorted((entry1, entry2) -> entry1.getKey().isAssignableFrom(entry2.getKey()) ? 1 : -1)
                .<Descriptor<? extends Q>>map(Map.Entry::getValue).toJavaOptional();

        return descriptor.map(descriptor1 -> descriptor1.describe(wrap(item)))
                .orElseGet(() -> this.defaultDescriptor.describe(wrap(item)));

    }
    //endregion

    //region Private Methods
    private <T> T wrap(Q item) {
        return (T)item;
    }
    //endregion

    //region Fields
    private Map<Class<?>, Descriptor<? extends Q>> descriptors;
    private Descriptor<? extends Q> defaultDescriptor;
    //endregion
}
