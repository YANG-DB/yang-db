package com.kayhut.fuse.rendering;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooby.MediaType;
import org.jooby.Renderer;

public abstract class JacksonBaseRenderer implements Renderer {
    //region Constructors
    public JacksonBaseRenderer(final ObjectMapper mapper, final MediaType type) {
        this.mapper = mapper;
        this.type = type;
    }
    //endregion

    //region Renderer Implementation
    public void render(final Object value, final Context ctx) throws Exception {
        if (ctx.accepts(this.type)) {
            ctx.type(this.type);
            this.renderValue(value, ctx);
        }

    }
    //endregion

    //region Abstract Methods
    protected abstract void renderValue(Object value, Context ctx) throws Exception;
    //endregion

    //region Properties
    public String name() {
        return "json";
    }

    public String toString() {
        return this.name();
    }
    //endregion

    //region Fields
    protected final ObjectMapper mapper;
    protected final MediaType type;
    //endregion
}
