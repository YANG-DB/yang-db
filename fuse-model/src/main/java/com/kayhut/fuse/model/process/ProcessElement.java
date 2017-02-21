package com.kayhut.fuse.model.process;

import com.kayhut.fuse.model.transport.Response;

/**
 * Created by lior on 20/02/2017.
 */
public interface ProcessElement<IN,OUT> {
    OUT process(IN input);

    class ProcessContext {

        //local response store
        private static ThreadLocal<Response> context = ThreadLocal.withInitial(() -> new Response("-1"));

        public static void set(Response response) {
            context.set(response);
        }

        public static Response get() {
            return context.get();
        }

    }

}
