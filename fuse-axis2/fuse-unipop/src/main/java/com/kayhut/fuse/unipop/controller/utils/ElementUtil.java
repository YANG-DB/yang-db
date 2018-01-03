package com.kayhut.fuse.unipop.controller.utils;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Optional;

/**
 * Created by Roman on 9/23/2017.
 */
public class ElementUtil {
    public static <V> Optional<V> value(Element element, String key){
        if (key.equals(T.id.getAccessor())) {
            return Optional.of((V)element.id());
        }

        if (element.keys().contains(key)) {
            return Optional.of(element.value(key));
        } else {
            return Optional.empty();
        }
    }
}
