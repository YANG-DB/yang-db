package com.kayhut.fuse.rendering;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooby.MediaType;
import org.jooby.Renderer;
import org.slf4j.Logger;

public class LoggingRenderer implements Renderer {
    //region Constructors
    public LoggingRenderer(Renderer renderer, Logger logger) {
        this.renderer = renderer;
        this.logger = logger;
    }
    //endregion

    //region Renderer Implementation
    public void render(final Object value, final Context ctx) throws Exception {
        this.renderer.render(value, ctx);
    }
    //endregion

    //region Properties
    public String name() {
        return this.renderer.name();
    }

    public String toString() {
        return this.name();
    }
    //endregion

    //region Fields
    protected Renderer renderer;
    protected Logger logger;
    //endregion
}
