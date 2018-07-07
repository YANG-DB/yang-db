package com.kayhut.fuse.dispatcher.cursor;

import com.google.inject.Inject;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.util.Map;
import java.util.Set;

public class CompositeCursorFactory implements CursorFactory {
    public static class Binding {
        //region Constructors
        public Binding(String type, Class<? extends CreateCursorRequest> klass, CursorFactory cursorFactory) {
            this.type = type;
            this.klass = klass;
            this.cursorFactory = cursorFactory;
        }
        //endregion

        //region Properties
        public String getType() {
            return this.type;
        }

        public Class<? extends CreateCursorRequest> getKlass() {
            return this.klass;
        }

        public CursorFactory getCursorFactory() {
            return this.cursorFactory;
        }
        //endregion

        //region fields
        private String type;
        private Class<? extends CreateCursorRequest> klass;
        private CursorFactory cursorFactory;
        //endregion
    }

    //region Constructors
    @Inject
    public CompositeCursorFactory(Set<Binding> bindings) {
        this.cursorFactories = Stream.ofAll(bindings)
                .toJavaMap(binding -> new Tuple2<>(binding.getKlass(), binding.getCursorFactory()));
    }
    //endregion

    //region CursorFactory Implementation
    @Override
    public Cursor createCursor(Context context) {
        CursorFactory cursorFactory = this.cursorFactories.get(context.getCursorRequest().getClass());
        if (cursorFactory == null) {
            throw new RuntimeException(String.format("Missing cursor factory binding for cursor requests of type %s",
                    context.getCursorRequest().getClass()));
        }

        return cursorFactory.createCursor(context);
    }
    //endregion

    //region Fields
    private Map<Class<? extends CreateCursorRequest>, CursorFactory> cursorFactories;
    //endregion
}
