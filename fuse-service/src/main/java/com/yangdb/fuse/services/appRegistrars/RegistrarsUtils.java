package com.yangdb.fuse.services.appRegistrars;

import com.yangdb.fuse.model.transport.ContentResponse;
import org.jooby.Request;
import org.jooby.Response;
import org.jooby.Result;
import org.jooby.Results;

import java.io.File;
import java.io.FileWriter;

public class RegistrarsUtils {

    /**
     * result is projected according to mime type
     * @param req
     * @param res
     * @param response
     * @return
     * @throws Throwable
     */
    protected static Result with(Request req, Response res, ContentResponse<Object> response) throws Throwable {
        //write content as temp file
        if (req.accepts("application/octet-stream").isPresent()) {
            File tempFile = File.createTempFile(response.getRequestId(), "-suffix");
            FileWriter writer = new FileWriter(tempFile);
            writer.write(response.getData().toString());
            writer.close();
            res.download(tempFile);
            tempFile.deleteOnExit();
        }
        return Results.with(response, response.status());
    }

}
