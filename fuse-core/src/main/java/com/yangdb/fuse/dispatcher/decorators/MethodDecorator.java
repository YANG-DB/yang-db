package com.yangdb.fuse.dispatcher.decorators;

/*-
 * #%L
 * fuse-core
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



import java.util.function.Function;
import java.util.function.Supplier;

public interface MethodDecorator<TResult, TResultContext> {
    interface ResultHandler<TResult, TResultContext> {
        TResult onSuccess(TResult result, TResultContext resultContext);
        TResult onFailure(Exception ex, TResultContext resultContext);

        class Standard<TResult, TResultContext> implements ResultHandler<TResult, TResultContext> {
            //region Static
            public static <TResult, TResultContext> Standard<TResult, TResultContext> getInstance() {
                return instance;
            }

            private static final Standard instance = new Standard();
            //endregion

            //region ResultHandler Implementation
            @Override
            public TResult onSuccess(TResult result, TResultContext resultContext) {
                return result;
            }

            @Override
            public TResult onFailure(Exception ex, TResultContext resultContext) {
                throw new RuntimeException(ex);
            }
            //endregion
        }

        class Passthrough<TResult, TResultContext> implements ResultHandler<TResult, TResultContext> {
            //region Constructors
            public Passthrough(Function<Exception, TResult> errorHandler) {
                this.errorHandler = errorHandler;
            }
            //endregion

            //region ResultHandler Implementation
            @Override
            public TResult onSuccess(TResult result, TResultContext resultContext) {
                return result;
            }

            @Override
            public TResult onFailure(Exception ex, TResultContext resultContext) {
                return this.errorHandler.apply(ex);
            }
            //endregion

            //region Fields
            private Function<Exception, TResult> errorHandler;
            //endregion
        }
    }

    TResult decorate(Supplier<TResult> methodInvocationSupplier, ResultHandler<TResult, TResultContext> resultHandler);

    default TResult decorate(Supplier<TResult> methodInvocationSupplier) {
        return decorate(methodInvocationSupplier, ResultHandler.Standard.getInstance());
    }
}
