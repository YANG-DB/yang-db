package com.kayhut.fuse.rendering;

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
