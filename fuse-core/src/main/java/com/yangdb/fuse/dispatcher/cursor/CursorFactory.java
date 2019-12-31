package com.yangdb.fuse.dispatcher.cursor;

/*-
 * #%L
 * fuse-core
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



import com.yangdb.fuse.dispatcher.resource.QueryResource;
import com.yangdb.fuse.model.transport.CreatePageRequest;
import com.yangdb.fuse.model.transport.cursor.CreateCursorRequest;
import com.yangdb.fuse.model.transport.cursor.LogicalGraphCursorRequest;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Roman on 02/04/2017.
 */
public interface CursorFactory {
    interface Context {
        QueryResource getQueryResource();

        CreateCursorRequest getCursorRequest();

        class Impl implements Context {
            //region Constructors
            public Impl(QueryResource queryResource, CreateCursorRequest cursorRequest) {
                this.queryResource = queryResource;
                this.cursorRequest = cursorRequest;
            }
            //endregion

            //region Context Implementation
            public QueryResource getQueryResource() {
                return queryResource;
            }

            public CreateCursorRequest getCursorRequest() {
                return cursorRequest;
            }
            //endregion

            //region Fields
            private QueryResource queryResource;
            private CreateCursorRequest cursorRequest;
            //endregion
        }
    }

    Cursor createCursor(Context context);

    /**
     * resolve cursor request class name by CursorType value
     * @param cursorTypeName
     * @return
     */
    static Class<? extends CreateCursorRequest> resolve(String cursorTypeName) {
        Reflections reflections = new Reflections(CreateCursorRequest.class.getPackage().getName());
        Set<Class<? extends CreateCursorRequest>> allClasses = reflections.getSubTypesOf(CreateCursorRequest.class);
        Optional<Class<? extends CreateCursorRequest>> cursorType = allClasses.stream().filter(clazz -> {
            try {
                //get value of static field member
                return clazz.getField("CursorType").get(null).equals(cursorTypeName);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
            return false;
        }).findFirst();

        return cursorType.isPresent() ? cursorType.get() : LogicalGraphCursorRequest.class;
    }

    /**
     * generate cursor request based on given params
     * @param cursorTypeName
     * @return
     */
    static CreateCursorRequest request(String cursorTypeName, CreatePageRequest pageRequest) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        Class<? extends CreateCursorRequest> cursor = resolve(cursorTypeName);
        Constructor<? extends CreateCursorRequest> constructor = cursor.getConstructor(pageRequest.getClass());
        return constructor.newInstance(pageRequest);
    }

}
