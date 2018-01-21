package com.kayhut.fuse.dispatcher.cursor;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;

/**
 * Created by roman.margolis on 07/01/2018.
 */
public class LoggingCursorFactory implements CursorFactory {
    public static final String cursorFactoryParameter = "LoggingCursorFactory.@cursorFactory";
    public static final String loggerParameter = "LoggingCursorFactory.@logger";

    //region Constructors
    @Inject
    public LoggingCursorFactory(
            @Named(cursorFactoryParameter) CursorFactory cursorFactory,
            @Named(loggerParameter) Logger logger) {
        this.cursorFactory = cursorFactory;
        this.logger = logger;
    }
    //endregion

    //region LoggingFactory Implementation
    @Override
    public Cursor createCursor(Context context) {
        return new LoggingCursor(this.cursorFactory.createCursor(context), this.logger);
    }
    //endregion

    //region Fields
    private CursorFactory cursorFactory;
    private Logger logger;
    //endregion
}
