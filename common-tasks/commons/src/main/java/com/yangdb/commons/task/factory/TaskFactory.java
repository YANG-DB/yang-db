package com.yangdb.commons.task.factory;

/*-
 * #%L
 * commons
 * %%
 * Copyright (C) 2016 - 2022 The YangDb Graph Database Project
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

import com.yangdb.commons.task.Task;

public interface TaskFactory {
    interface Uni<T1, TResult> {
        Task<TResult, ?> getTask(T1 param1) throws Exception;
    }

    interface Bi<T1, T2, TResult> {
        Task<TResult, ?> getTask(T1 param1, T2 param2) throws Exception;
    }

    interface Tri<T1, T2, T3, TResult> {
        Task<TResult, ?> getTask(T1 param1, T2 param2, T3 param3) throws Exception;
    }

    interface Quadri<T1, T2, T3, T4, TResult> {
        Task<TResult, ?> getTask(T1 param1, T2 param2, T3 param3, T4 param4) throws Exception;
    }
}
