package com.yangdb.fuse.unipop.schemaProviders.indexPartitions;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
import java.util.Optional;

/**
 * Created by Roman on 11/05/2017.
 */
public class StaticIndexPartitions implements IndexPartitions {
    //region Constructors
    public StaticIndexPartitions(String...indices) {
        this(Stream.of(indices));
    }

    public StaticIndexPartitions(Iterable<String> indices) {
        this.indices = Stream.ofAll(indices).toJavaList();
    }
    //endregion

    //region IndexPartitions Implementation
    @Override
    public Optional<String> getPartitionField() {
        return Optional.empty();
    }

    @Override
    public Iterable<Partition> getPartitions() {
        return Collections.singletonList(() -> this.indices);
    }
    //endregion

    //region Fields
    private Iterable<String> indices;
    //endregion
}
