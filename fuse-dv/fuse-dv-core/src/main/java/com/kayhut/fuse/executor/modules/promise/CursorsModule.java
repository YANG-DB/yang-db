package com.kayhut.fuse.executor.modules.promise;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import com.kayhut.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.executor.cursor.promise.TraversalCursor;
import com.kayhut.fuse.model.transport.cursor.CreatePathsCursorRequest;
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
