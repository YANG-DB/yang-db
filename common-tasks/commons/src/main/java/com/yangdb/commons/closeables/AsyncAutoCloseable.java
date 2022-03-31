package com.yangdb.commons.closeables;

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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface AsyncAutoCloseable extends AutoCloseable{
    CompletionStage<Void> closeAsync() throws Exception;
    default void close() throws Exception {
        this.closeAsync().toCompletableFuture().join();
    }

    class Noop implements AsyncAutoCloseable {
        public static Noop instance = new Noop();

        //region AsyncAutoCloseable Implementation
        @Override
        public CompletableFuture<Void> closeAsync() throws Exception {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void close() throws Exception {}
        //endregion
    }

    class Blocking implements AsyncAutoCloseable {
        //region Constructors
        public Blocking(AutoCloseable closeable) {
            this.closeable = closeable;
        }
        //endregion

        //region AsyncAutoCloseable Implementation
        @Override
        public CompletableFuture<Void> closeAsync() throws Exception {
            this.close();
            return CompletableFuture.completedFuture(null);
        }
        @Override
        public void close() throws Exception {
            if (this.closeable != null) {
                this.closeable.close();
            }
        }
        //endregion

        //region Fields
        private final AutoCloseable closeable;
        //endregion
    }

    class Supplier implements AsyncAutoCloseable {
        //region Constructors
        public Supplier(java.util.function.Supplier<AsyncAutoCloseable> closeableSupplier) {
            this.closeableSupplier = closeableSupplier;
        }
        //endregion

        //region AsyncAutoCloseable Implementation
        @Override
        public CompletionStage<Void> closeAsync() throws Exception {
            return this.closeableSupplier.get().closeAsync();
        }
        //endregion

        //region Fields
        private final java.util.function.Supplier<AsyncAutoCloseable> closeableSupplier;
        //endregion
    }
}
