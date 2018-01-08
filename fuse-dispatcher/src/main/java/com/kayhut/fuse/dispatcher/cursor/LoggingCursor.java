package com.kayhut.fuse.dispatcher.cursor;

import com.kayhut.fuse.dispatcher.gta.LoggingPlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.model.results.QueryResult;
import org.slf4j.Logger;

/**
 * Created by roman.margolis on 07/01/2018.
 */
public class LoggingCursor implements Cursor {
    //region Constructors
    public LoggingCursor(Cursor cursor, Logger logger) {
        this.cursor = cursor;
        this.logger = logger;
    }
    //endregion

    //region Cursor Implementation
    @Override
    public QueryResult getNextResults(int numResults) {
        boolean thrownException = false;

        try {
            this.logger.trace("start getNextResults");
            return this.cursor.getNextResults(numResults);
        } catch (Exception ex) {
            thrownException = true;
            this.logger.error("failed getNextResults");
            throw ex;
        } finally {
            if (!thrownException) {
                this.logger.trace("finish getNextResults");
            }
        }
    }
    //endregion

    //region Fields
    private Cursor cursor;
    private Logger logger;
    //endregion
}
