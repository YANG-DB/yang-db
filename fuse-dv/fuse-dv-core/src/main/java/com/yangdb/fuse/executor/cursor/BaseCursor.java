package com.yangdb.fuse.executor.cursor;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2021 The YangDb Graph Database Project
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

import com.yangdb.fuse.dispatcher.cursor.Cursor;
import com.yangdb.fuse.dispatcher.provision.CursorRuntimeProvision;

public abstract class BaseCursor implements Cursor<TraversalCursorContext> {
    protected TraversalCursorContext context;
    protected CursorRuntimeProvision runtimeProvision;

    public BaseCursor(TraversalCursorContext context) {
        this.context = context;
        this.runtimeProvision = context.getRuntimeProvision();
    }

    @Override
    public int getActiveScrolls() {
        return runtimeProvision.getActiveScrolls();
    }

    @Override
    public boolean clearScrolls() {
        return runtimeProvision.clearScrolls();
    }

    public TraversalCursorContext getContext() {
        return context;
    }

}
