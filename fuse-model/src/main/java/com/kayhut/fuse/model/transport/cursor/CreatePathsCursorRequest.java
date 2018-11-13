package com.kayhut.fuse.model.transport.cursor;

/*-
 * #%L
 * CreatePathsCursorRequest.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.transport.CreatePageRequest;

/**
 * Created by roman.margolis on 11/03/2018.
 */
public class CreatePathsCursorRequest extends CreateCursorRequest {
    public static final String CursorType = "paths";

    //region Constructors
    public CreatePathsCursorRequest() {
        super(CursorType);
    }

    public CreatePathsCursorRequest(CreatePageRequest createPageRequest) {
        super(CursorType, createPageRequest);
    }

    public CreatePathsCursorRequest(Include include, CreatePageRequest createPageRequest) {
        super(CursorType, include, createPageRequest);
    }
    //endregion
}
