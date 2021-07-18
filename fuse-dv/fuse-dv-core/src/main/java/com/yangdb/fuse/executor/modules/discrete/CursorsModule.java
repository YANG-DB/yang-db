package com.yangdb.fuse.executor.modules.discrete;

/*-
 * #%L
 * fuse-dv-core
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

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import com.yangdb.fuse.dispatcher.cursor.CompositeCursorFactory.Binding;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.executor.cursor.discrete.*;
import com.yangdb.fuse.model.transport.cursor.*;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by Roman on 7/7/2018.
 */
public class CursorsModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreatePathsCursorRequest.CursorType,
                CreatePathsCursorRequest.class,
                new PathsTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateForwardOnlyPathTraversalCursorRequest.CursorType,
                CreateForwardOnlyPathTraversalCursorRequest.class,
                new ForwardOnlyPathsTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CountCursorRequest.CursorType,
                CountCursorRequest.class,
                new CountTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                FindPathTraversalCursorRequest.CursorType,
                FindPathTraversalCursorRequest.class,
                new FindPathsTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateGraphCursorRequest.CursorType,
                CreateGraphCursorRequest.class,
                new GraphTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateGraphQLCursorRequest.CursorType,
                CreateGraphQLCursorRequest.class,
                new GraphQLTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateGraphHierarchyCursorRequest.CursorType,
                CreateGraphHierarchyCursorRequest.class,
                new NewGraphHierarchyTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateHierarchyFlattenCursorRequest.CursorType,
                CreateHierarchyFlattenCursorRequest.class,
                new HierarchyFlattenCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateInnerQueryCursorRequest.CursorType,
                CreateInnerQueryCursorRequest.class,
                new InnerQueryCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateCsvCursorRequest.CursorType,
                CreateCsvCursorRequest.class,
                new CsvTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                ProjectionCursorRequest.CursorType,
                ProjectionCursorRequest.class,
                new IndexProjectionCursor.Factory()));
    }
    //endregion
}
