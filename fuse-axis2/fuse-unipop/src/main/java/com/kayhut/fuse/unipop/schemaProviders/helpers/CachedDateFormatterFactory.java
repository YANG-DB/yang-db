package com.kayhut.fuse.unipop.schemaProviders.helpers;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class CachedDateFormatterFactory implements DateFormatterFactory {
    //region Constructor
    public CachedDateFormatterFactory() {
        this.dateFormatters = new HashMap<>();
    }
    //endregion

    //region DateFormatterFactory Implementation
    @Override
    public SimpleDateFormat getDateFormatter(String format) {
        SimpleDateFormat sdf = this.dateFormatters.get(format);
        if (sdf == null) {
            sdf = new SimpleDateFormat(format);
            this.dateFormatters.put(format, sdf);
        }

        return sdf;
    }
    //endregion

    //region Fields
    private Map<String, SimpleDateFormat> dateFormatters;
    //endregion
}
