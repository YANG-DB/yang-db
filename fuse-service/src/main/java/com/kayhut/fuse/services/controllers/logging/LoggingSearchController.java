package com.kayhut.fuse.services.controllers.logging;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateQueryRequest;
import com.kayhut.fuse.services.controllers.SearchController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingSearchController implements SearchController{
    public static final String injectionName = "LoggingSearchController.inner";

    //region Constructors
    @Inject
    public LoggingSearchController(@Named(injectionName)SearchController controller) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.controller = controller;
    }
    //endregion

    //region SearchController Implementation
    @Override
    public ContentResponse search(CreateQueryRequest request) {
        boolean thrownException = false;

        try {
            this.logger.debug("start search");
            return controller.search(request);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed search: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.debug("finish search");
            }
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private SearchController controller;
    //endregion
}
