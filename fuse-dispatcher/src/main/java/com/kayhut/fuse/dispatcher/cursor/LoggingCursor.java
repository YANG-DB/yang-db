package com.kayhut.fuse.dispatcher.cursor;

import com.kayhut.fuse.dispatcher.gta.LoggingPlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.model.results.QueryResult;
import org.slf4j.Logger;

import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.LogType.finish;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.LogType.start;

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
            new LogMessage(this.logger, trace, start, "getNextResults", "start getNextResults").log();
            return this.cursor.getNextResults(numResults);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage(this.logger, error, finish, "getNextResults", "failed getNextResults", ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage(this.logger, trace, finish, "getNextResults", "finish getNextResults").log();
            }
        }
    }
    //endregion

    //region Fields
    private Cursor cursor;
    private Logger logger;
    //endregion
}
