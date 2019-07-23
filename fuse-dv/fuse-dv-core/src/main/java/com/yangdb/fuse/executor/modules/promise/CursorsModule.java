package com.yangdb.fuse.executor.modules.promise;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
import com.yangdb.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.executor.cursor.promise.TraversalCursor;
import com.yangdb.fuse.model.transport.cursor.CreatePathsCursorRequest;
import com.typesafe.config.Config;
import org.jooby.Env;

/**
 * Created by Roman on 7/7/2018.
 */
public class CursorsModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config config, Binder binder) throws Throwable {
        Multibinder.newSetBinder(binder, CompositeCursorFactory.Binding.class).addBinding().toInstance(new CompositeCursorFactory.Binding(
                CreatePathsCursorRequest.CursorType,
                CreatePathsCursorRequest.class,
                new TraversalCursor.Factory()));
    }
    //endregion
}
