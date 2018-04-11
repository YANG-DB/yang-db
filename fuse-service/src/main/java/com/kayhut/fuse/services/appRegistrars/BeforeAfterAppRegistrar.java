package com.kayhut.fuse.services.appRegistrars;

import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.kayhut.fuse.model.results.TextContent;
import com.kayhut.fuse.model.transport.ExternalMetadata;
import com.kayhut.fuse.services.suppliers.RequestExternalMetadataSupplier;
import org.jooby.*;

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
    private void registerBeforeHandlers(Jooby app) {
        app.before((req, resp) -> bindExternalMetadataSupplier(req));
    }

    private void registerAfterHandlers(Jooby app) {
        app.after((req, resp, result) ->  {
            addExternalMetadataResponseHeaders(req, resp);
            return result;
        });

        app.after((req, resp, result) ->  {
            Object content = result.get();
            if(req.type().name().equals(MediaType.plain.name()) && TextContent.class.isAssignableFrom(content.getClass())){
                result.set(((TextContent)content).content());
                result.type(MediaType.plain);
            }
            return result;
        });
    }

    private void bindExternalMetadataSupplier(Request request) {
        Optional<String> id = request.header(FUSE_EXTERNAL_ID_HEADER).toOptional();
        Optional<String> operation = request.header(FUSE_EXTERNAL_OPERATION_HEADER).toOptional();

        if (id.isPresent() || operation.isPresent()) {
            request.set(RequestExternalMetadataSupplier.class,
                    new RequestExternalMetadataSupplier.Impl(
                            new ExternalMetadata(id.orElse(null), operation.orElse(null))));
        }
    }

    private void addExternalMetadataResponseHeaders(Request request, Response response) {
        Optional<String> id = request.header(FUSE_EXTERNAL_ID_HEADER).toOptional();
        Optional<String> operation = request.header(FUSE_EXTERNAL_OPERATION_HEADER).toOptional();

        id.ifPresent(id1 -> response.header(FUSE_EXTERNAL_ID_HEADER, id1));
        operation.ifPresent(operation1 -> response.header(FUSE_EXTERNAL_OPERATION_HEADER, operation1));
    }
    //endregion

    //region Fields
    private static final String FUSE_EXTERNAL_ID_HEADER = "fuse-external-id";
    private static final String FUSE_EXTERNAL_OPERATION_HEADER = "fuse-external-operation";
    //endregion
}
