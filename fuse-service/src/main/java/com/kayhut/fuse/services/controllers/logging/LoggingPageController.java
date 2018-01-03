package com.kayhut.fuse.services.controllers.logging;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.logging.ElapsedConverter;
import com.kayhut.fuse.model.resourceInfo.PageResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.services.controllers.PageController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingPageController implements PageController {
    public static final String injectionName = "LoggingPageController.inner";

    //region Constructors
    @Inject
    public LoggingPageController(@Named(injectionName)PageController controller) {
        this.logger = LoggerFactory.getLogger(controller.getClass());
        this.controller = controller;
    }
    //endregion

    //region PageController Implementation
    @Override
    public ContentResponse<PageResourceInfo> create(String queryId, String cursorId, CreatePageRequest createPageRequest) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start create");
            return controller.create(queryId, cursorId, createPageRequest);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed create", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish create");
            }
        }
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryId, String cursorId) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getInfo");
            return controller.getInfo(queryId, cursorId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getInfo", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getInfo");
            }
        }
    }

    @Override
    public ContentResponse<PageResourceInfo> getInfo(String queryId, String cursorId, String pageId) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getInfo");
            return controller.getInfo(queryId, cursorId, pageId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getInfo", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getInfo");
            }
        }
    }

    @Override
    public ContentResponse<Object> getData(String queryId, String cursorId, String pageId) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getData");
            return controller.getData(queryId, cursorId, pageId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getData", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getData");
            }
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private PageController controller;
    //endregion
}
