package com.yangdb.fuse.executor.ontology.schema.load;

import com.yangdb.fuse.model.GlobalConstants;
import org.junit.Assert;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class DataLoaderUtilsTest {

    private static SimpleDateFormat utcFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat(GlobalConstants.DEFAULT_DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf;
    }

    @Test
    public void parsesNegativeEpochMillisAsPre1970Date() {
        Assert.assertTrue(DataLoaderUtils.validateValue("date", "-733082855765", utcFormat()));
        Assert.assertEquals("1946-10-09 05:52:24.235", DataLoaderUtils.parseValue("date", "-733082855765", utcFormat()));
    }

    @Test
    public void parsesPositiveEpochMillis() {
        Assert.assertTrue(DataLoaderUtils.validateValue("date", "792843774684", utcFormat()));
        Assert.assertEquals("1995-02-15 10:22:54.684", DataLoaderUtils.parseValue("date", "792843774684", utcFormat()));
    }

    @Test
    public void keepsParsingFormattedDates() {
        Assert.assertEquals("1909-03-28 23:04:48.177", DataLoaderUtils.parseValue("date", "1909-03-28 23:04:48.177", utcFormat()));
        Assert.assertFalse(DataLoaderUtils.validateValue("date", "not a date", utcFormat()));
    }
}
