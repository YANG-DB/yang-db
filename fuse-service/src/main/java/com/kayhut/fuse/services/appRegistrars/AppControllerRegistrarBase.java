package com.kayhut.fuse.services.appRegistrars;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.google.inject.TypeLiteral;
import org.jooby.*;

public abstract class AppControllerRegistrarBase<TController> implements AppRegistrar{
    //region Constructors
    public AppControllerRegistrarBase(Class<? extends TController> klass) {
        this.klass = klass;
    }

    public AppControllerRegistrarBase(TypeLiteral<TController> typeLiteral) {
        this.typeLiteral = typeLiteral;
    }
    //endregion

    //region Protected Methods
    protected TController getController(Jooby app) {
        if (this.klass != null) {
            return app.require(this.klass);
        }

        if (this.typeLiteral != null) {
            return app.require(this.typeLiteral);
        }

        return null;
    }


    //endregion

    //region Fields
    private Class<? extends TController> klass;
    private TypeLiteral<TController> typeLiteral;
    //endregion
}
