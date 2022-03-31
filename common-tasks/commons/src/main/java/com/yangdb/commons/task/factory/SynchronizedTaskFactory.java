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

import com.yangdb.commons.task.SynchronizedTask;
import com.yangdb.commons.task.Task;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class SynchronizedTaskFactory {
    public static class Uni<T1, TResult> implements TaskFactory.Uni<T1, TResult> {
        //region Constructors
        public Uni(TaskFactory.Uni<T1, TResult> taskFactory, Lock lock, long lockAcquireTimeout, TimeUnit lockAcquireTimeUnit) {
            this.taskFactory = taskFactory;
            this.lock = lock;
            this.lockAcquireTimeout = lockAcquireTimeout;
            this.lockAcquireTimeUnit = lockAcquireTimeUnit;
        }
        //endregion

        //region TaskFactory Implementation
        @Override
        public Task<TResult, ?> getTask(T1 param1) throws Exception {
            return new SynchronizedTask<>(
                    this.taskFactory.getTask(param1),
                    this.lock,
                    this.lockAcquireTimeout,
                    this.lockAcquireTimeUnit);
        }
        //endregion

        //region Fields
        private final TaskFactory.Uni<T1, TResult> taskFactory;
        private final Lock lock;
        private final long lockAcquireTimeout;
        private final TimeUnit lockAcquireTimeUnit;
        //endregion
    }

    public static class Bi<T1, T2, TResult> implements TaskFactory.Bi<T1, T2, TResult> {
        //region Constructors
        public Bi(TaskFactory.Bi<T1, T2, TResult> taskFactory, Lock lock, long lockAcquireTimeout, TimeUnit lockAcquireTimeUnit) {
            this.taskFactory = taskFactory;
            this.lock = lock;
            this.lockAcquireTimeout = lockAcquireTimeout;
            this.lockAcquireTimeUnit = lockAcquireTimeUnit;
        }
        //endregion

        //region TaskFactory Implementation
        @Override
        public Task<TResult, ?> getTask(T1 param1, T2 param2) throws Exception {
            return new SynchronizedTask<>(
                    this.taskFactory.getTask(param1, param2),
                    this.lock,
                    this.lockAcquireTimeout,
                    this.lockAcquireTimeUnit);
        }
        //endregion

        //region Fields
        private final TaskFactory.Bi<T1, T2, TResult> taskFactory;
        private final Lock lock;
        private final long lockAcquireTimeout;
        private final TimeUnit lockAcquireTimeUnit;
        //endregion
    }

    public static class Tri<T1, T2, T3, TResult> implements TaskFactory.Tri<T1, T2, T3, TResult> {
        //region Constructors
        public Tri(TaskFactory.Tri<T1, T2, T3, TResult> taskFactory, Lock lock, long lockAcquireTimeout, TimeUnit lockAcquireTimeUnit) {
            this.taskFactory = taskFactory;
            this.lock = lock;
            this.lockAcquireTimeout = lockAcquireTimeout;
            this.lockAcquireTimeUnit = lockAcquireTimeUnit;
        }
        //endregion

        //region TaskFactory Implementation
        @Override
        public Task<TResult, ?> getTask(T1 param1, T2 param2, T3 param3) throws Exception {
            return new SynchronizedTask<>(
                    this.taskFactory.getTask(param1, param2, param3),
                    this.lock,
                    this.lockAcquireTimeout,
                    this.lockAcquireTimeUnit);
        }
        //endregion

        //region Fields
        private final TaskFactory.Tri<T1, T2, T3, TResult> taskFactory;
        private final Lock lock;
        private final long lockAcquireTimeout;
        private final TimeUnit lockAcquireTimeUnit;
        //endregion
    }

    public static class Quadri<T1, T2, T3, T4, TResult> implements TaskFactory.Quadri<T1, T2, T3, T4, TResult> {
        //region Constructors
        public Quadri(TaskFactory.Quadri<T1, T2, T3, T4, TResult> taskFactory, Lock lock, long lockAcquireTimeout, TimeUnit lockAcquireTimeUnit) {
            this.taskFactory = taskFactory;
            this.lock = lock;
            this.lockAcquireTimeout = lockAcquireTimeout;
            this.lockAcquireTimeUnit = lockAcquireTimeUnit;
        }
        //endregion

        //region TaskFactory Implementation
        @Override
        public Task<TResult, ?> getTask(T1 param1, T2 param2, T3 param3, T4 param4) throws Exception {
            return new SynchronizedTask<>(
                    this.taskFactory.getTask(param1, param2, param3, param4),
                    this.lock,
                    this.lockAcquireTimeout,
                    this.lockAcquireTimeUnit);
        }
        //endregion

        //region Fields
        private final TaskFactory.Quadri<T1, T2, T3, T4, TResult> taskFactory;
        private final Lock lock;
        private final long lockAcquireTimeout;
        private final TimeUnit lockAcquireTimeUnit;
        //endregion
    }
}
