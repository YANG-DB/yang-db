package com.kayhut.fuse.executor.logging;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.cursor.LoggingCursor;
import com.kayhut.fuse.executor.cursor.TraversalCursorContext;
import org.slf4j.Logger;

/**
 * Created by roman.margolis on 07/01/2018.
 */
public class LoggingCursorFactory implements CursorFactory {
    public static final String cursorFactoryParameter = "LoggingCursorFactory.@cursorFactory";
    public static final String cursorLoggerParameter = "LoggingCursorFactory.@cursorLogger";
    public static final String traversalLoggerParameter = "LoggingCursorFactory.@traversalLogger";

    //region Constructors
    @Inject
    public LoggingCursorFactory(
            @Named(cursorFactoryParameter) CursorFactory cursorFactory,
            @Named(cursorLoggerParameter) Logger cursorLogger,
            @Named(traversalLoggerParameter) Logger traversalLogger) {
        this.cursorFactory = cursorFactory;
        this.cursorLogger = cursorLogger;
        this.traversalLogger = traversalLogger;
    }
    //endregion

    //region LoggingFactory Implementation
    @Override
    public Cursor createCursor(Context context) {
        TraversalCursorContext traversalCursorContext = (TraversalCursorContext)context;
        TraversalCursorContext loggingTraversalCursorContext = new TraversalCursorContext(
                traversalCursorContext.getOntology(),
                traversalCursorContext.getQueryResource(),
                traversalCursorContext.getCursorType(),
                new LoggingTraversal<>(traversalCursorContext.getTraversal(), this.traversalLogger));
        return new LoggingCursor(this.cursorFactory.createCursor(loggingTraversalCursorContext), this.cursorLogger);
    }
    //endregion

    //region Fields
    private CursorFactory cursorFactory;
    private Logger cursorLogger;
    private Logger traversalLogger;
    //endregion
}
