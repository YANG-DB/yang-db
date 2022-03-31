package com.yangdb.commons.function;

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

import com.yangdb.commons.function.function.ThrowingBiFunction;
import com.yangdb.commons.function.function.ThrowingFunction;
import com.yangdb.commons.function.function.ThrowingQuatriFunction;
import com.yangdb.commons.function.function.ThrowingTriFunction;
import com.yangdb.commons.function.supplier.ThrowingSupplier;
import com.yangdb.commons.throwables.ThrowableUtils;
import com.yangdb.commons.util.GenericUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public interface MethodDecorator<TResult, TResultContext> {
    default TResult decorate(ThrowingSupplier<TResult> methodInvocationSupplier) throws Exception {
        return decorate(methodInvocationSupplier, ResultHandler.Standard.getInstance());
    }
    TResult decorate(ThrowingSupplier<TResult> methodInvocationSupplier, ResultHandler<TResult, TResultContext> resultHandler) throws Exception;

    class Passthrough<TResult, TResultContext> implements MethodDecorator<TResult, TResultContext> {
        //region Static
        public static <TResult, TResultContext> Passthrough<TResult, TResultContext> getInstance() {
            return GenericUtils.infere(instance);
        }
        private final static Passthrough<?, ?> instance = new Passthrough<>();
        //endregion

        //region MethodDecorator Implementation
        @Override
        public TResult decorate(ThrowingSupplier<TResult> methodInvocationSupplier, ResultHandler<TResult, TResultContext> resultHandler) throws Exception {
            try {
                return resultHandler.onSuccess(methodInvocationSupplier.get(), null);
            } catch (Exception ex) {
                return resultHandler.onFailure(ex, null);
            }
        }
        //endregion
    }

    interface Unary<TIn, TResult, TResultContext> {
        default TResult decorate(TIn in, ThrowingFunction<TIn, TResult> methodInvocationFunction) throws Exception {
            return decorate(in, methodInvocationFunction, ResultHandler.Standard.getInstance());
        }
        TResult decorate(TIn in, ThrowingFunction<TIn, TResult> methodInvocationFunction, ResultHandler<TResult, TResultContext> resultHandler) throws Exception;

        class Passthrough<TIn, TResult, TResultContext> implements Unary<TIn, TResult, TResultContext> {
            //region Static
            public static <TIn, TResult, TResultContext> Passthrough<TIn, TResult, TResultContext> getInstance() {
                return GenericUtils.infere(instance);
            }
            private final static Passthrough<?, ?, ?> instance = new Passthrough<>();
            //endregion

            //region MethodDecorator Implementation
            @Override
            public TResult decorate(TIn in, ThrowingFunction<TIn, TResult> methodInvocationSupplier, ResultHandler<TResult, TResultContext> resultHandler) throws Exception {
                try {
                    return resultHandler.onSuccess(methodInvocationSupplier.apply(in), null);
                } catch (Exception ex) {
                    return resultHandler.onFailure(ex, null);
                }
            }
            //endregion
        }
    }

    interface Binary<TIn1, TIn2, TResult, TResultContext> {
        default TResult decorate(TIn1 in1, TIn2 in2, ThrowingBiFunction<TIn1, TIn2, TResult> methodInvocationFunction) throws Exception {
            return decorate(in1, in2, methodInvocationFunction, ResultHandler.Standard.getInstance());
        }
        TResult decorate(TIn1 in1, TIn2 in2, ThrowingBiFunction<TIn1, TIn2, TResult> methodInvocationFunction, ResultHandler<TResult, TResultContext> resultHandler) throws Exception;

        class Passthrough<TIn1, TIn2, TResult, TResultContext> implements Binary<TIn1, TIn2, TResult, TResultContext> {
            //region Static
            public static <TIn1, TIn2, TResult, TResultContext> Passthrough<TIn1, TIn2, TResult, TResultContext> getInstance() {
                return GenericUtils.infere(instance);
            }
            private final static Passthrough<?, ?, ?, ?> instance = new Passthrough<>();
            //endregion

            //region MethodDecorator Implementation
            @Override
            public TResult decorate(TIn1 in1, TIn2 in2, ThrowingBiFunction<TIn1, TIn2, TResult> methodInvocationSupplier, ResultHandler<TResult, TResultContext> resultHandler) throws Exception {
                try {
                    return resultHandler.onSuccess(methodInvocationSupplier.apply(in1, in2), null);
                } catch (Exception ex) {
                    return resultHandler.onFailure(ex, null);
                }
            }
            //endregion
        }
    }

    interface Trinary<TIn1, TIn2, TIn3, TResult, TResultContext> {
        default TResult decorate(TIn1 in1, TIn2 in2, TIn3 in3, ThrowingTriFunction<TIn1, TIn2, TIn3, TResult> methodInvocationFunction) throws Exception {
            return decorate(in1, in2, in3, methodInvocationFunction, ResultHandler.Standard.getInstance());
        }
        TResult decorate(TIn1 in1, TIn2 in2, TIn3 in3, ThrowingTriFunction<TIn1, TIn2, TIn3, TResult> methodInvocationFunction, ResultHandler<TResult, TResultContext> resultHandler) throws Exception;

        class Passthrough<TIn1, TIn2, TIn3, TResult, TResultContext> implements Trinary<TIn1, TIn2, TIn3, TResult, TResultContext> {
            //region Static
            public static <TIn1, TIn2, TIn3, TResult, TResultContext> Passthrough<TIn1, TIn2, TIn3, TResult, TResultContext> getInstance() {
                return GenericUtils.infere(instance);
            }
            private final static Passthrough<?, ?, ?, ?, ?> instance = new Passthrough<>();
            //endregion

            //region MethodDecorator Implementation
            @Override
            public TResult decorate(TIn1 in1, TIn2 in2, TIn3 in3, ThrowingTriFunction<TIn1, TIn2, TIn3, TResult> methodInvocationSupplier, ResultHandler<TResult, TResultContext> resultHandler) throws Exception {
                try {
                    return resultHandler.onSuccess(methodInvocationSupplier.apply(in1, in2, in3), null);
                } catch (Exception ex) {
                    return resultHandler.onFailure(ex, null);
                }
            }
            //endregion
        }
    }

    interface Quaternary<TIn1, TIn2, TIn3, TIn4, TResult, TResultContext> {
        default TResult decorate(TIn1 in1, TIn2 in2, TIn3 in3, TIn4 in4, ThrowingQuatriFunction<TIn1, TIn2, TIn3, TIn4, TResult> methodInvocationFunction) throws Exception {
            return decorate(in1, in2, in3, in4, methodInvocationFunction, ResultHandler.Standard.getInstance());
        }
        TResult decorate(TIn1 in1, TIn2 in2, TIn3 in3, TIn4 in4, ThrowingQuatriFunction<TIn1, TIn2, TIn3, TIn4, TResult> methodInvocationFunction, ResultHandler<TResult, TResultContext> resultHandler) throws Exception;

        class Passthrough<TIn1, TIn2, TIn3, TIn4, TResult, TResultContext> implements Quaternary<TIn1, TIn2, TIn3, TIn4, TResult, TResultContext> {
            //region Static
            public static <TIn1, TIn2, TIn3, TIn4, TResult, TResultContext> Passthrough<TIn1, TIn2, TIn3, TIn4, TResult, TResultContext> getInstance() {
                return GenericUtils.infere(instance);
            }
            private final static Passthrough<?, ?, ?, ?, ?, ?> instance = new Passthrough<>();
            //endregion

            //region MethodDecorator Implementation
            @Override
            public TResult decorate(TIn1 in1, TIn2 in2, TIn3 in3, TIn4 in4, ThrowingQuatriFunction<TIn1, TIn2, TIn3, TIn4, TResult> methodInvocationSupplier, ResultHandler<TResult, TResultContext> resultHandler) throws Exception {
                try {
                    return resultHandler.onSuccess(methodInvocationSupplier.apply(in1, in2, in3, in4), null);
                } catch (Exception ex) {
                    return resultHandler.onFailure(ex, null);
                }
            }
            //endregion
        }
    }

    interface Async<TResult, TResultContext> {
        default CompletionStage<TResult> decorate(ThrowingSupplier<CompletionStage<TResult>> methodInvocationSupplier) throws Exception{
            return decorate(methodInvocationSupplier, ResultHandler.Standard.getInstance());
        }
        CompletionStage<TResult> decorate(ThrowingSupplier<CompletionStage<TResult>> methodInvocationSupplier, ResultHandler<TResult, TResultContext> resultHandler) throws Exception;

        class Passthrough<TResult, TResultContext> implements Async<TResult, TResultContext> {
            //region Static
            public static <TResult, TResultContext> Passthrough<TResult, TResultContext> getInstance() {
                return GenericUtils.infere(instance);
            }
            private final static Passthrough<?, ?> instance = new Passthrough<>();
            //endregion

            //region Async Implementation
            @Override
            public CompletionStage<TResult> decorate(ThrowingSupplier<CompletionStage<TResult>> methodInvocationSupplier, ResultHandler<TResult, TResultContext> resultHandler) throws Exception {
                try {
                    return methodInvocationSupplier.get()
                            .thenApply(methodInvocationResult -> {
                                try {
                                    return resultHandler.onSuccess(methodInvocationResult, null);
                                } catch (Exception ex) {
                                    throw ThrowableUtils.toRuntimeException(ex);
                                }
                            });
                } catch (Exception ex) {
                    return CompletableFuture.completedFuture(resultHandler.onFailure(ex, null));
                }
            }
            //endregion
        }

        interface Unary<TIn, TResult, TResultContext> {
            default CompletionStage<TResult> decorate(TIn in, ThrowingFunction<TIn, CompletionStage<TResult>> methodInvocationSupplier) throws Exception{
                return decorate(in, methodInvocationSupplier, ResultHandler.Standard.getInstance());
            }
            CompletionStage<TResult> decorate(TIn in, ThrowingFunction<TIn, CompletionStage<TResult>> methodInvocationSupplier,  ResultHandler<TResult, TResultContext> resultHandler) throws Exception;

            class Passthrough<TIn, TResult, TResultContext> implements Unary<TIn, TResult, TResultContext> {
                //region Static
                public static <TIn, TResult, TResultContext> Passthrough<TIn, TResult, TResultContext> getInstance() {
                    return GenericUtils.infere(instance);
                }
                private final static Passthrough<?, ?, ?> instance = new Passthrough<>();
                //endregion

                //region Async Implementation
                @Override
                public CompletionStage<TResult> decorate(TIn in, ThrowingFunction<TIn, CompletionStage<TResult>> methodInvocationSupplier, ResultHandler<TResult, TResultContext> resultHandler) throws Exception {
                    try {
                        return methodInvocationSupplier.apply(in)
                                .thenApply(methodInvocationResult -> {
                                    try {
                                        return resultHandler.onSuccess(methodInvocationResult, null);
                                    } catch (Exception ex) {
                                        throw ThrowableUtils.toRuntimeException(ex);
                                    }
                                });
                    } catch (Exception ex) {
                        return CompletableFuture.completedFuture(resultHandler.onFailure(ex, null));
                    }
                }
                //endregion
            }
        }

        interface Binary<TIn1, TIn2, TResult, TResultContext> {
            default CompletionStage<TResult> decorate(TIn1 in1, TIn2 in2, ThrowingBiFunction<TIn1, TIn2, CompletionStage<TResult>> methodInvocationSupplier) throws Exception{
                return decorate(in1, in2, methodInvocationSupplier, ResultHandler.Standard.getInstance());
            }
            CompletionStage<TResult> decorate(TIn1 in1, TIn2 in2, ThrowingBiFunction<TIn1, TIn2, CompletionStage<TResult>> methodInvocationSupplier,  ResultHandler<TResult, TResultContext> resultHandler) throws Exception;

            class Passthrough<TIn1, TIn2, TResult, TResultContext> implements Binary<TIn1, TIn2, TResult, TResultContext> {
                //region Static
                public static <TIn1, TIn2, TResult, TResultContext> Passthrough<TIn1, TIn2, TResult, TResultContext> getInstance() {
                    return GenericUtils.infere(instance);
                }
                private final static Passthrough<?, ?, ?, ?> instance = new Passthrough<>();
                //endregion

                //region Async Implementation
                @Override
                public CompletionStage<TResult> decorate(TIn1 in1, TIn2 in2, ThrowingBiFunction<TIn1, TIn2, CompletionStage<TResult>> methodInvocationSupplier, ResultHandler<TResult, TResultContext> resultHandler) throws Exception {
                    try {
                        return methodInvocationSupplier.apply(in1, in2)
                                .thenApply(methodInvocationResult -> {
                                    try {
                                        return resultHandler.onSuccess(methodInvocationResult, null);
                                    } catch (Exception ex) {
                                        throw ThrowableUtils.toRuntimeException(ex);
                                    }
                                });
                    } catch (Exception ex) {
                        return CompletableFuture.completedFuture(resultHandler.onFailure(ex, null));
                    }
                }
                //endregion
            }
        }
    }

    interface ResultHandler<TResult, TResultContext> {
        TResult onSuccess(TResult result, TResultContext resultContext) throws Exception;
        TResult onFailure(Throwable ex, TResultContext resultContext) throws Exception;

        class Standard<TResult, TResultContext> implements ResultHandler<TResult, TResultContext> {
            //region Static
            public static <TResult, TResultContext> Standard<TResult, TResultContext> getInstance() {
                return GenericUtils.infere(instance);
            }

            private static final Standard<?, ?> instance = new Standard<>();
            //endregion

            //region ResultHandler Implementation
            @Override
            public TResult onSuccess(TResult result, TResultContext resultContext) {
                return result;
            }

            @Override
            public TResult onFailure(Throwable ex, TResultContext resultContext) {
                throw ThrowableUtils.toRuntimeException(ex);
            }
            //endregion
        }

        class Passthrough<TResult, TResultContext> implements ResultHandler<TResult, TResultContext> {
            //region Constructors
            public Passthrough(Function<Throwable, TResult> errorHandler) {
                this.errorHandler = errorHandler;
            }
            //endregion

            //region ResultHandler Implementation
            @Override
            public TResult onSuccess(TResult result, TResultContext resultContext) {
                return result;
            }

            @Override
            public TResult onFailure(Throwable ex, TResultContext resultContext) {
                return this.errorHandler.apply(ex);
            }
            //endregion

            //region Fields
            private final Function<Throwable, TResult> errorHandler;
            //endregion
        }
    }
}
