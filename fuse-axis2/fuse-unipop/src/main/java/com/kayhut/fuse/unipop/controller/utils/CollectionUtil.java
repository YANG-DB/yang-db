package com.kayhut.fuse.unipop.controller.utils;

import javaslang.collection.Stream;

import java.util.List;

/**
 * Created by User on 19/03/2017.
 */
public class CollectionUtil {
    public static <T> List<T> listFromObjectValue(Object value) {
        if (Iterable.class.isAssignableFrom(value.getClass())) {
            return Stream.ofAll((Iterable)value).map(o -> (T)o).toJavaList();
        } else if (value.getClass().isArray()) {
            return Stream.of((T[])value).toJavaList();
        } else {
            return Stream.of((T)value).toJavaList();
        }
    }
}
