package com.kayhut.fuse.services.controllers.logging;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.kayhut.fuse.services.controllers.CursorController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingCursorController implements CursorController {
    public static final String injectionName = "LoggingCursorController.inner";

    //region Constructors
    @Inject
    public LoggingCursorController(@Named(injectionName)CursorController controller) {
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.controller = controller;
    }
    //endregion

    //region CursorController Implementation
    @Override
    public ContentResponse<CursorResourceInfo> create(String queryId, CreateCursorRequest createCursorRequest) {
        boolean thrownException = false;

        try {
            this.logger.debug("start create");
            return controller.create(queryId, createCursorRequest);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed create: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.debug("finish create");
            }
        }
    }

    @Override
    public ContentResponse<StoreResourceInfo> getInfo(String queryId) {
        boolean thrownException = false;

        try {
            this.logger.debug("start getInfo");
            return controller.getInfo(queryId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getInfo: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.debug("finish getInfo");
            }
        }
    }

    @Override
    public ContentResponse<CursorResourceInfo> getInfo(String queryId, String cursorId) {
        boolean thrownException = false;

        try {
            this.logger.debug("start getInfo");
            return controller.getInfo(queryId, cursorId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getInfo: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.debug("finish getInfo");
            }
        }
    }

    @Override
    public ContentResponse<Boolean> delete(String queryId, String cursorId) {
        boolean thrownException = false;

        try {
            this.logger.debug("start delete");
            return controller.delete(queryId, cursorId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed delete: {}", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.debug("finish delete");
            }
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private CursorController controller;
    //endregion
}
