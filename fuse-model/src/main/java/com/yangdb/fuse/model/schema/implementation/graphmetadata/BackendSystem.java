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
 * Backend system type used in a certain Implementation level file.
 *
 *
 */
public enum BackendSystem {

    /***
     * Relational: This option indicates that the implementation level will map the abstraction layer property graph to
     * to a relational backend (e.g. RDBMS)
     */
    RELATIONAL,

    /***
     * Index: This option indicates that the implementation level will map the abstraction layer property graph to
     * to an index backend.
     */
    INDEX
}
