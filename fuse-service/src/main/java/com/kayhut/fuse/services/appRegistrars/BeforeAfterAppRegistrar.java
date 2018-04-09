package com.kayhut.fuse.services.appRegistrars;

import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.services.suppliers.ExternalRequestIdSupplier;
import org.jooby.Jooby;
import org.jooby.Request;
import org.jooby.Response;

import java.util.Optional;

public class BeforeAfterAppRegistrar implements AppRegistrar {
    //region AppRegistrar Implementation
    @Override
    public void register(Jooby app, AppUrlSupplier appUrlSupplier) {
        registerBeforeHandlers(app);
        registerAfterHandlers(app);
    }
    //endregion

    //region Private Methods
    private void bindExternalIdProvider(Request request) {
        Optional<String> externalId = request.header(FUSE_EXTERNAL_ID_HEADER).toOptional();
        externalId.ifPresent(id -> request.set(ExternalRequestIdSupplier.class, new ExternalRequestIdSupplier.Impl(id)));
    }

    private void addExternalIdResponseHeader(Request request, Response response) {
        Optional<String> externalId = request.header(FUSE_EXTERNAL_ID_HEADER).toOptional();
        externalId.ifPresent(id -> response.header(FUSE_EXTERNAL_ID_HEADER, id));
    }

    private void registerBeforeHandlers(Jooby app) {
        app.before((req, resp) -> bindExternalIdProvider(req));
    }

    private void registerAfterHandlers(Jooby app) {
        app.after((req, resp, result) ->  {
            addExternalIdResponseHeader(req, resp);
            return result;
        });
    }
    //endregion

    //region Fields
    private static final String FUSE_EXTERNAL_ID_HEADER = "fuse-external-id";
    //endregion
}
