package com.kayhut.fuse.unipop.controller.utils;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;

/**
 * Created by Roman on 9/23/2017.
 */
public class ElementUtil {
    public static <V> V value(Element element, String key){
        if (key.equals(T.id.getAccessor())) {
            return (V)element.id();
        }

        return element.value(key);
    }
}
