package com.yangdb.fuse.dispatcher.driver.execute;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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

import com.yangdb.fuse.model.transport.CreateQueryRequestMetadata;

import java.util.List;

public interface QueryStrategyRegistrar<T extends CreateQueryRequestMetadata> {
    /**
     * get all query execution strategies
     * @return
     */
    List<QueryDriverStrategy> register();

    /**
     * test for the specific (first) strategy that can handle the query request
     * @param request
     * @return
     */
    QueryDriverStrategy<T> apply(T request) ;
}
