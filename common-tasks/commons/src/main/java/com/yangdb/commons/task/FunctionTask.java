package com.yangdb.commons.task;

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

import java.time.Clock;
import java.util.Optional;
import java.util.function.Function;

public class FunctionTask<TResult, TContext> extends BaseTask<TResult, TContext> {
    //region Constructors
    public FunctionTask(Function<Task<TResult, TContext>, Optional<TResult>> function) {
        this(0, null, Clock.systemUTC(), function);
    }

    public FunctionTask(Clock clock, Function<Task<TResult, TContext>, Optional<TResult>> function) {
        this(0, null, clock, function);
    }

    public FunctionTask(String type, Function<Task<TResult, TContext>, Optional<TResult>> function) {
        this(0, type, Clock.systemUTC(), function);
    }

    public FunctionTask(String type, Clock clock, Function<Task<TResult, TContext>, Optional<TResult>> function) {
        this(0, type, clock, function);
    }

    public FunctionTask(int id, String type, Clock clock, Function<Task<TResult, TContext>, Optional<TResult>> function) {
        super(id, type, clock);
        this.function = function;
    }
    //endregion

    //region Override Methods
    @Override
    protected void execute(Task.State previousState) {
        if (this.function != null) {
            try {
                Optional<TResult> optionalResult = this.function.apply(this);
                if (optionalResult != null && optionalResult.isPresent()) {
                    this.complete(optionalResult.get());
                }
            } catch (Exception ex) {
                this.complete(ex);
            }
        } else {
            this.complete((TResult)null);
        }
    }
    //endregion

    //region Fields
    private final Function<Task<TResult, TContext>, Optional<TResult>> function;
    //endregion
}
