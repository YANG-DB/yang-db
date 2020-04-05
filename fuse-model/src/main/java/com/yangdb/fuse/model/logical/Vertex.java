package com.yangdb.fuse.model.logical;

/*-
 * #%L
 * fuse-model
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

import java.util.Map;

public interface Vertex<V extends Vertex> {
    /**
     *
     * @return
     */
    String id();

    /**
     *
     * @return
     */
    String label();

    /**
     *
     * @param label
     * @return
     */
    V label(String label);

    /**
     *
     * @return
     */
    String tag();

    /**
     *
     * @param tag
     * @return
     */
    V tag(String tag);

    /**
     *
     * @param entity
     * @return
     */
    V merge(V entity);

    /**
     *
     * @return
     */
    Map<String,Object> metadata();

    /**
     *
     * @return
     */
    Map<String,Object> fields();

}
