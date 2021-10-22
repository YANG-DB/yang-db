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



import com.yangdb.fuse.dispatcher.provision.CursorRuntimeProvision;
import com.yangdb.fuse.model.results.QueryResultBase;

/**
 * Created by lior.perry on 06/03/2017.
 */
public interface Cursor<T> extends CursorRuntimeProvision {
    QueryResultBase getNextResults(int numResults);
    T getContext();

}
