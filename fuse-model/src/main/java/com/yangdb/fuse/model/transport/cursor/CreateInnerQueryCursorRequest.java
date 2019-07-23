package com.yangdb.fuse.model.transport.cursor;

/*-
 * #%L
 * CreatePathsCursorRequest.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb
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

/**
 * Created by lior perry
 */
public class CreateInnerQueryCursorRequest extends CreateCursorRequest {
    public static final String CursorType = "inner";
    private CreateCursorRequest cursorRequest;

    //region Constructors
    public CreateInnerQueryCursorRequest() {
        super(CursorType);
    }

    public CreateInnerQueryCursorRequest(CreateCursorRequest cursorRequest) {
        this();
        this.cursorRequest = cursorRequest;
    }
    //endregion

    public CreateCursorRequest getCursorRequest() {
        return cursorRequest;
    }
}
