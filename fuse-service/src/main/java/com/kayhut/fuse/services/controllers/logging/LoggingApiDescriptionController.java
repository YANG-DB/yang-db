package com.kayhut.fuse.services.controllers.logging;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.logging.ElapsedConverter;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.services.controllers.ApiDescriptionController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingApiDescriptionController implements ApiDescriptionController {
    public static final String injectionName = "LoggingApiDescriptionController.inner";

    //region Constructors
    @Inject
    public LoggingApiDescriptionController(@Named(injectionName)ApiDescriptionController controller) {
        this.logger = LoggerFactory.getLogger(controller.getClass());
        this.controller = controller;
    }
    //endregion

    //region ApiDescriptionController Implementation
    @Override
    public ContentResponse<FuseResourceInfo> getInfo() {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getInfo");
            return controller.getInfo();
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getInfo: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getInfo");
            }
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private ApiDescriptionController controller;
    //endregion
}
