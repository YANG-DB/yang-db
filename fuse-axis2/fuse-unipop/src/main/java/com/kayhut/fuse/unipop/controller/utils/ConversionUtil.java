package com.kayhut.fuse.unipop.controller.utils;

import javaslang.collection.Stream;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Roman on 17/05/2017.
 */
public class ConversionUtil {
    public static SimpleDateFormat sdf;
    static  {
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }


    public static <S, T> T prepareValue(S value) {
        if (value instanceof Date) {
            return (T)sdf.format((Date)value);
        } else if (Iterable.class.isAssignableFrom(value.getClass())) {
            return (T)Stream.ofAll(CollectionUtil.listFromObjectValue(value))
                    .map(ConversionUtil::prepareValue)
                    .toJavaList();
        }

        return (T)value;
    }
}
