package com.kayhut.test.etl;

/*-
 * #%L
 * fuse-domain-dragons-test
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

import java.util.Map;

/**
 * Created by Roman on 06/06/2017.
 */
public class ChunkPartitioner implements Partitioner {
    //region Constructors
    public ChunkPartitioner(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
        this.currentPartition = 1;
        this.currentChunkSize = 0;
    }
    //endregion

    //region Partitioner Implementation
    @Override
    public String getPartition(Map<String, String> document) {
        this.currentChunkSize++;
        if (this.currentChunkSize == this.maxChunkSize) {
            this.currentPartition++;
            this.currentChunkSize = 0;
        }

        return Integer.toString(this.currentPartition);
    }
    //endregion

    //region Fields
    private int currentPartition;
    private int currentChunkSize;
    private int maxChunkSize;
    //endregion
}
