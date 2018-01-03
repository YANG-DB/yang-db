package com.kayhut.fuse.services.controllers.logging;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.logging.ElapsedConverter;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.StoreResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.kayhut.fuse.services.controllers.CursorController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Created by roman.margolis on 14/12/2017.
 */
public class LoggingCursorController implements CursorController {
    public static final String injectionName = "LoggingCursorController.inner";

    //region Constructors
    @Inject
    public LoggingCursorController(@Named(injectionName)CursorController controller) {
        this.logger = LoggerFactory.getLogger(controller.getClass());
        this.controller = controller;
    }
    //endregion

    //region CursorController Implementation
    @Override
    public ContentResponse<CursorResourceInfo> create(String queryId, CreateCursorRequest createCursorRequest) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start create");
            return controller.create(queryId, createCursorRequest);
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
    public ContentResponse<StoreResourceInfo> getInfo(String queryId) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start getInfo");
            return controller.getInfo(queryId);
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
    public ContentResponse<CursorResourceInfo> getInfo(String queryId, String cursorId) {
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
    public ContentResponse<Boolean> delete(String queryId, String cursorId) {
        MDC.put(ElapsedConverter.key, Long.toString(System.currentTimeMillis()));
        boolean thrownException = false;

        try {
            this.logger.trace("start delete");
            return controller.delete(queryId, cursorId);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed delete", ex);
            return null;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish delete");
            }
        }
    }
    //endregion

    //region Fields
    private Logger logger;
    private CursorController controller;
    //endregion
}
