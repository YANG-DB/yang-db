package com.fuse.domain.knowledge.datagen.dataSuppliers;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
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

import java.util.Map;

/**
 * Created by Roman on 6/23/2018.
 */
public class WeightedIntSupplier extends RandomDataSupplier<Integer> {
    //region Constructors
    public WeightedIntSupplier(Map<Integer, Integer> valueCounts) {
        long total = Stream.ofAll(valueCounts.values()).sum().longValue();
        Stream.ofAll(valueCounts.entrySet())
                .sortBy(entry -> (double)entry.getValue() / (double)total);

    }
    //endregion

    //region RandomDataSupplier Implementation
    @Override
    public Integer get() {
        return null;
    }
    //endregion

    //region Fields
    //endregion
}
