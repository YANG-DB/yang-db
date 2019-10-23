package com.yangdb.fuse.executor.ontology.schema;

/*-
 *
 * fuse-dv-core
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

import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;

import java.util.List;

/**
 * Created by lior.perry on 2/11/2018.
 *
 * Describing the elastic (raw) indices & indices partitions
 * each index has id formatting
 */
public interface RawSchema extends IndicesProvider {

    IndexPartitions getPartition(String type);

    String getIdFormat(String type);

    String getPrefix(String type);

    String getIdPrefix(String type);

    List<IndexPartitions.Partition> getPartitions(String type);

}
