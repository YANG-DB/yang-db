package com.kayhut.fuse.services.suppliers;

/*-
 * #%L
 * fuse-service
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

/**
 * Created by Roman on 4/9/2018.
 */
public class CachedRequestIdSupplier implements RequestIdSupplier {
    public static final String RequestIdSupplierParameter = "CachedRequestIdSupplier.@requestIdSupplier";

    //region Constructors
    @Inject
    public CachedRequestIdSupplier(
            @Named(RequestIdSupplierParameter) RequestIdSupplier requestIdSupplier) {
        this.requestIdSupplier = requestIdSupplier;
    }
    //endregion

    //region RequestIdSupplier Implementation
    @Override
    public String get() {
        if (this.requestId == null) {
            this.requestId = this.requestIdSupplier.get();
        }

        return this.requestId;
    }
    //endregion

    //region Fields
    private RequestIdSupplier requestIdSupplier;
    private String requestId;
    //endregion
}
