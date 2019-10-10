package com.yangdb.fuse.dispatcher.cursor;

/*-
 *
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.google.inject.Inject;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
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
