package com.yangdb.fuse.model.resourceInfo;

/*-
 * #%L
 * fuse-model
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

public class ResultResourceInfo<T> extends ResourceInfoBase {

    public ResultResourceInfo(String resourceUrl, String resourceId, T result) {
        super(resourceUrl, resourceId);
        this.result = result;
    }

    public ResultResourceInfo(String resourceUrl, String resourceId, FuseError error) {
        super(resourceUrl, resourceId);
        this.error = error;
    }

    public T getResult() {
        return result;
    }

    public FuseError getError() {
        return error;
    }

    public boolean isError() {
        return error!=null;
    }

    private FuseError error;
    private T result;

}
