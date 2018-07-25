package com.kayhut.fuse.dispatcher.decorators;

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
