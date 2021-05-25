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

/***
 * Size of the data granularity used to verify if something is coexisting on the same graph snapshot.
 *
 *
 */
public enum SnapshotConcatenationUnit {

    /***
     * All tuples that happen within a <b>second</b> are considered has coexisting on a graph snapshot.
     */
    SECONDS,

    /***
     * All tuples that happen within a <b>minute</b> are considered has coexisting on a graph snapshot.
     */
    MINUTES,

    /***
     * All tuples that happen within a <b>hour</b> are considered has coexisting on a graph snapshot.
     */
    HOURS,

    /***
     * All tuples that happen within a <b>day</b> are considered has coexisting on a graph snapshot.
     */
    DAYS,

    /***
     * All tuples that happen within a <b>week</b> are considered has coexisting on a graph snapshot.
     */
    WEEKS,

    /***
     * All tuples that happen within a <b>month</b> are considered has coexisting on a graph snapshot.
     */
    MONTHS,

    /***
     * All tuples that happen within a <b>year</b> are considered has coexisting on a graph snapshot.
     */
    YEARS
}
