package com.yangdb.fuse.unipop.controller.common.appender;

import com.yangdb.fuse.unipop.controller.common.context.BulkContext;
import javaslang.collection.Stream;

public interface EdgeUtils {

    static String getLabel(BulkContext context, String _default) {
        if (context.isEmpty()) return _default;
        return Stream.ofAll(context.getBulkVertices()).get(0).label();
    }

}
