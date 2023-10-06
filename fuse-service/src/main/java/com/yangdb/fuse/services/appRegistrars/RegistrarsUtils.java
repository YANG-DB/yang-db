package com.yangdb.fuse.services.appRegistrars;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.resourceInfo.CursorResourceInfo;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.resourceInfo.PageResourceInfo;
import com.yangdb.fuse.model.resourceInfo.QueryResourceInfo;
import com.yangdb.fuse.model.results.CsvQueryResult;
import com.yangdb.fuse.model.results.TextContent;
import com.yangdb.fuse.model.transport.ContentResponse;
import org.jooby.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RegistrarsUtils {

    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String TEXT_CSV = "text/csv";
    public static final String IMAGE_SVG_XML = "image/svg+xml";
    //json fast serializer
    public static ObjectMapper mapper = new ObjectMapper();

    /**
     * result is projected according to mime type
     *
     * @param req
     * @param res
     * @param response
     * @return
     * @throws Throwable
     */
    public static void with(Request req, Response res, ContentResponse<Object> response) throws Throwable {
        if (response.getData() instanceof FuseError) {
            //return error (log)
            res.status(Status.SERVER_ERROR);
            res.type(MediaType.json);
            res.send(response);
        }

        //write content as temp file
        if (req.accepts(APPLICATION_OCTET_STREAM).isPresent()) {
            res.type(MediaType.octetstream);
            File tempFile = Files.createTempFile(response.getRequestId(), "-suffix").toFile();
            FileWriter writer = new FileWriter(tempFile);
            writer.write(response.getData().toString());
            writer.close();
            tempFile.deleteOnExit();
            res.status(response.status().getStatus());
            //has to be last
            res.download(tempFile);
        } else if (req.accepts(TEXT_CSV).isPresent()) {
            res.type(MediaType.text);
            String now = Instant.now().toString();
            File tempFile = Files.createTempFile("csv_" + now, ".csv").toFile();

            QueryResourceInfo queryResourceInfo = (QueryResourceInfo) response.getData();
            if (!queryResourceInfo.getCursorResourceInfos().isEmpty() && !queryResourceInfo.getCursorResourceInfos().get(0).getPageResourceInfos().isEmpty()) {
                //get only the data content from the page resource
                Object element = ((queryResourceInfo).getCursorResourceInfos().get(0)).getPageResourceInfos().get(0).getData();
                String content = element.toString();
                if (element instanceof TextContent) {
                    content = ((TextContent) element).content();
                }
                Files.write(tempFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
                tempFile.deleteOnExit();
                res.status(response.status().getStatus());
                //has to be last
                res.download(tempFile);
            }
        } else if (req.accepts(APPLICATION_JSON).isPresent()) {
            res.type(MediaType.json);
            res.status(response.status().getStatus());
        } else if (req.accepts(IMAGE_SVG_XML).isPresent()) {
            ((File) response.getData()).deleteOnExit();
            res.status(response.status().getStatus());
            //has to be last
            res.download((File) response.getData());
        }

        //default response
        res.status(response.status().getStatus());
        res.type(MediaType.json);
        res.send(response);
    }

    protected static Result withImg(Request req, Response res, ContentResponse<File> response) throws Throwable {
        //write content as temp file
        if (req.accepts(IMAGE_SVG_XML).isPresent()) {
            res.download(response.getData());
            res.status(response.status().getStatus());
            response.getData().deleteOnExit();
        }
        return Results.with(response, response.status().getStatus());
    }


}
