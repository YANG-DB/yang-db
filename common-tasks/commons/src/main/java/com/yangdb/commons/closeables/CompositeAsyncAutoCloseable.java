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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CompositeAsyncAutoCloseable implements AsyncAutoCloseable {
    public enum Method {
        sequential,
        parallel
    }

    //region Constructors
    public CompositeAsyncAutoCloseable(AsyncAutoCloseable...closeables) {
        this(Arrays.asList(closeables));
    }

    public CompositeAsyncAutoCloseable(Collection<AsyncAutoCloseable> closeables) {
        this(Method.parallel, closeables);
    }

    public CompositeAsyncAutoCloseable(Method method, AsyncAutoCloseable...closeables) {
        this(method, Arrays.asList(closeables));
    }

    public CompositeAsyncAutoCloseable(Method method, Collection<AsyncAutoCloseable> closeables) {
        this.method = method;
        this.closeables = closeables;
    }
    //endregion

    //region AsyncAutoCloseable Implementation
    @Override
    public CompletionStage<Void> closeAsync() throws Exception {
        if (this.closeables.iterator().hasNext()) {
            if (this.method == Method.sequential) {
                return this.sequentialCloseAsync();
            }

            return this.parallelCloseAsync();
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }
    //endregion

    //region Private Methods
    private CompletionStage<Void> sequentialCloseAsync() throws Exception {
        CompletionStage<Void> sequentialFuture = null;
        for(AsyncAutoCloseable closeable : this.closeables) {
            sequentialFuture = sequentialFuture == null ?
                    closeable.closeAsync() :
                    sequentialFuture.thenComposeAsync((v) -> {
                        try {
                            return closeable.closeAsync();
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    });
        }
        return sequentialFuture;
    }

    private CompletionStage<Void> parallelCloseAsync() throws Exception {
        List<CompletionStage<Void>> futures = new ArrayList<>();

        for(AsyncAutoCloseable closeable : this.closeables) {
            futures.add(closeable.closeAsync());
        }

        return CompletableFuture.allOf(futures.<CompletableFuture<Void>>toArray(new CompletableFuture[0]));
    }
    //endregion

    //region Fields
    private final Method method;
    private final Iterable<AsyncAutoCloseable> closeables;
    //endregion
}
