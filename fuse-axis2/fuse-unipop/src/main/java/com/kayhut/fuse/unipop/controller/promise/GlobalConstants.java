package com.kayhut.fuse.unipop.controller.promise;

/**
 * Created by User on 19/03/2017.
 */
public class GlobalConstants {
    public static class HasKeys {
        public static final String PROMISE = "promise";
        public static final String CONSTRAINT = "constraint";
        public static final String DIRECTION = "direction";
        public static final String COUNT = "count";
    }

    public static class Labels {
        public static final String PROMISE = "promise";
        public static final String PROMISE_FILTER = "promiseFilter";
        public static final String NONE = "_none_";
    }

    public static class EdgeSchema {
        public static String SOURCE_ID = "entityA.id";
        public static String DEST_ID = "entityB.id";
        public static String SOURCE = "entityA";
        public static String DEST = "entityB";
    }
}
