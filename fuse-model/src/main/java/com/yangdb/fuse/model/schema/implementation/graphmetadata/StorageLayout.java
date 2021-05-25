package com.yangdb.fuse.model.schema.implementation.graphmetadata;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2021 The YangDb Graph Database Project
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

/**
 * The possible formats that may be used when storing temporal data.
 *
 *
 */
public enum StorageLayout {

    /***
     * Which means that all tuples contained between the intervals that define a grain should be
     * considered has coexisting on the same graph snapshot.
     */
    SNAPSHOT,

    /***
     * Disables temporal graph evaluations.
     */
    IGNORETIME,

    /***
     * Assume delta updates on the graph data, as a continuous information stream..
     */
    DELTA
}
