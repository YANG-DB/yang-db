package com.kayhut.fuse.unipop.controller.common.converter;

import java.util.Map;

/**
 * Created by roman.margolis on 14/03/2018.
 */
public interface DataItem {
    Object id();
    Map<String, Object> properties();
}
