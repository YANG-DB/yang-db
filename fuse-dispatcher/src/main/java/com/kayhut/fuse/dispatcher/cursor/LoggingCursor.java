package com.kayhut.fuse.dispatcher.cursor;

import com.kayhut.fuse.dispatcher.logging.ElapsedFrom;
import com.kayhut.fuse.dispatcher.logging.LogMessage;
import com.kayhut.fuse.dispatcher.logging.LogType;
import com.kayhut.fuse.dispatcher.logging.MethodName;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.results.QueryResultBase;
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
    public QueryResultBase getNextResults(int numResults) {
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
