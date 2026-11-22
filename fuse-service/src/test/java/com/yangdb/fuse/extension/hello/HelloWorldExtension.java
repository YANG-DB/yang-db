package com.yangdb.fuse.extension.hello;

import com.google.inject.Module;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.extension.QueryLanguages;
import com.yangdb.fuse.extension.YangDbExtension;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.services.appRegistrars.AppRegistrar;
import org.jooby.Jooby;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A complete extension, as an outside contributor would write it: a toy query language and a toy route, listed in
 * {@code META-INF/services/com.yangdb.fuse.extension.YangDbExtension}. Nothing else: no config, no core edit.
 */
public class HelloWorldExtension implements YangDbExtension {
    public static final String LANGUAGE = "echo";

    @Override
    public List<Module> modules() {
        return Collections.singletonList(binder -> QueryLanguages.register(binder, LANGUAGE).to(EchoTransformer.class));
    }

    @Override
    public List<AppRegistrar> registrars() {
        return Collections.singletonList(new HelloRegistrar());
    }

    /** "translates" a query into an empty ASG query named after the query text */
    public static class EchoTransformer implements QueryTransformer<QueryInfo<String>, AsgQuery> {
        @Override
        public AsgQuery transform(QueryInfo<String> query) {
            return AsgQuery.Builder.start(query.getQuery(), query.getOntology()).build();
        }
    }

    /** GET /hello */
    public static class HelloRegistrar implements AppRegistrar {
        /** how often a HelloRegistrar was registered, to prove the service registers it exactly once */
        public static final AtomicInteger REGISTRATIONS = new AtomicInteger();

        @Override
        public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
            REGISTRATIONS.incrementAndGet();
            app.get("/hello", () -> "hello from an extension");
        }
    }
}
