package com.kayhut.fuse.model.process;

import com.kayhut.fuse.model.transport.ContentResponse;

/**
 * Created by lior on 20/02/2017.
 */
public interface ProcessElement<IN,OUT> {
    OUT process(IN input);

    class ProcessContext {

        //local response store
        private static ThreadLocal<ContentResponse> context = ThreadLocal.withInitial(() -> new ContentResponse("-1"));

        public static void set(ContentResponse response) {
            context.set(response);
        }

        public static ContentResponse get() {
            return context.get();
        }

    }

}
