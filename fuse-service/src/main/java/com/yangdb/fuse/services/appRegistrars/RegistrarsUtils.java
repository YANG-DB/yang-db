package com.yangdb.fuse.services.appRegistrars;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
