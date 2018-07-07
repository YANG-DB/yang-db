package com.kayhut.fuse.executor.modules.discrete;

import com.google.inject.Binder;
import com.google.inject.multibindings.Multibinder;
import com.kayhut.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.kayhut.fuse.dispatcher.cursor.CompositeCursorFactory.Binding;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.kayhut.fuse.executor.CursorBindingModule;
import com.kayhut.fuse.executor.cursor.discrete.*;
import com.kayhut.fuse.model.transport.cursor.*;
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
                CreateGraphCursorRequest.CursorType,
                CreateGraphCursorRequest.class,
                new GraphTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateGraphHierarchyCursorRequest.CursorType,
                CreateGraphHierarchyCursorRequest.class,
                new NewGraphHierarchyTraversalCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateHierarchyFlattenCursorRequest.CursorType,
                CreateHierarchyFlattenCursorRequest.class,
                new HierarchyFlattenCursor.Factory()));

        Multibinder.newSetBinder(binder, Binding.class).addBinding().toInstance(new Binding(
                CreateCsvCursorRequest.CursorType,
                CreateCsvCursorRequest.class,
                new CsvTraversalCursor.Factory()));
    }
    //endregion
}
