package com.kayhut.fuse.services.controllers.logging;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.logging.ElapsedConverter;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.services.controllers.SearchController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingSearchController implements SearchController{
    public static final String injectionName = "LoggingSearchController.inner";

    //region Constructors
    @Inject
    public LoggingSearchController(@Named(injectionName)SearchController controller) {
        this.logger = LoggerFactory.getLogger(controller.getClass());
        this.controller = controller;
    }
    //endregion

    //region SearchController Implementation
    @Override
    public ContentResponse search(CreateQueryRequest request) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start search");
            return controller.search(request);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed search", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish search");
            }
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private SearchController controller;
    //endregion
}
