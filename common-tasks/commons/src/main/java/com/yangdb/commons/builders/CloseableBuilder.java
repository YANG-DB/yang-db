package com.yangdb.commons.builders;

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

import com.yangdb.commons.closeables.AsyncAutoCloseable;
import com.yangdb.commons.closeables.CompositeAsyncAutoCloseable;
import com.yangdb.commons.closeables.CompositeAutoCloseable;

import java.util.*;

public abstract class CloseableBuilder<T> implements GenericBuilder<T> {
    //region Constructors
    public CloseableBuilder() {
        this.closeables = Collections.emptySet();
        this.asyncCloseables = Collections.emptySet();
    }
    //endregion

    //region Public Methods
    public <TBuilder extends CloseableBuilder<T>> TBuilder with() {
        this.acceptingCloseables = false;
        return (TBuilder)this;
    }

    public <TBuilder extends CloseableBuilder<T>> TBuilder withCloseables() {
        this.acceptingCloseables = true;
        return (TBuilder)this;
    }
    //endregion

    //region Protected Methods
    protected void collectCloseable(Object closeable) {
        if (closeable instanceof AutoCloseable) {
            if (this.acceptingCloseables) {
                this.ensureCloseables();
                if (closeable instanceof AsyncAutoCloseable) {
                    this.asyncCloseables.add((AsyncAutoCloseable) closeable);
                    this.closeables.remove(closeable);
                } else {
                    this.closeables.add((AutoCloseable)closeable);
                }
            }
        }
    }

    protected List<AsyncAutoCloseable> getAsyncAutoCloseables() {
        return Arrays.asList(
                new AsyncAutoCloseable.Blocking(new CompositeAutoCloseable(this.closeables)),
                new CompositeAsyncAutoCloseable(this.asyncCloseables));
    }
    //endregion

    //region Private Methods
    protected void ensureCloseables() {
        if (this.closeables == Collections.<AutoCloseable>emptySet()) {
            this.closeables = new LinkedHashSet<>();
            this.asyncCloseables = new LinkedHashSet<>();
        }
    }
    //endregion

    //region Fields
    private boolean acceptingCloseables;
    protected Set<AutoCloseable> closeables;
    protected Set<AsyncAutoCloseable> asyncCloseables;
    //endregion
}
