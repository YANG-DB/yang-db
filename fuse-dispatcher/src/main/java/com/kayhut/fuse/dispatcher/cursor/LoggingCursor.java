package com.kayhut.fuse.dispatcher.cursor;

import com.kayhut.fuse.dispatcher.gta.LoggingPlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.logging.ElapsedFrom;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.dispatcher.logging.LogType;
import com.kayhut.fuse.dispatcher.logging.MethodName;
import com.kayhut.fuse.dispatcher.resource.CursorResource;
import com.kayhut.fuse.model.results.QueryResult;
import org.slf4j.Logger;

import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.kayhut.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.kayhut.fuse.dispatcher.logging.LogType.*;

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
            new LogMessage.Impl(this.logger, trace, "start getNextResults", LogType.of(start), getNextResults, ElapsedFrom.now()).log();
            return this.cursor.getNextResults(numResults);
        } catch (Exception ex) {
            thrownException = true;
            new LogMessage.Impl(this.logger, error, "failed getNextResults", LogType.of(failure), getNextResults, ElapsedFrom.now())
                    .with(ex).log();
            throw ex;
        } finally {
            if (!thrownException) {
                new LogMessage.Impl(this.logger, trace, "finish getNextResults", LogType.of(success), getNextResults, ElapsedFrom.now()).log();
            }
        }
    }
    //endregion

    //region Fields
    private Cursor cursor;
    private Logger logger;

    private static MethodName.MDCWriter getNextResults = MethodName.of("getNextResults");
    //endregion
}
