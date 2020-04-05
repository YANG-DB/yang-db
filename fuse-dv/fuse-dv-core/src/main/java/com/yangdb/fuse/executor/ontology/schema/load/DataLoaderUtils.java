package com.yangdb.fuse.executor.ontology.schema.load;

/*-
 * #%L
 * fuse-dv-core
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

import com.yangdb.fuse.executor.ontology.DataTransformer;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import javaslang.collection.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;
import org.geojson.Point;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.zip.InflaterInputStream;

public interface DataLoaderUtils {

    /**
     * init E/S indices templates
     * @param client
     * @param schema
     * @return
     * @throws IOException
     */
    static long init(Client client, RawSchema schema) throws IOException {
        String workingDir = System.getProperty("user.dir");
        File templates = Paths.get(workingDir, "indexTemplates").toFile();
        File[] templateFiles = templates.listFiles();
        if (templateFiles != null) {
            for (File templateFile : templateFiles) {
                String templateName = FilenameUtils.getBaseName(templateFile.getName());
                String template = FileUtils.readFileToString(templateFile, "utf-8");
                if (!client.admin().indices().getTemplates(new GetIndexTemplatesRequest(templateName)).actionGet().getIndexTemplates().isEmpty()) {
                    final AcknowledgedResponse acknowledgedResponse = client.admin().indices().deleteTemplate(new DeleteIndexTemplateRequest(templateName)).actionGet(1500);
                    if (!acknowledgedResponse.isAcknowledged()) return -1;
                }
                final AcknowledgedResponse acknowledgedResponse = client.admin().indices().putTemplate(new PutIndexTemplateRequest(templateName).source(template, XContentType.JSON)).actionGet(1500);
                if (!acknowledgedResponse.isAcknowledged()) return -1;
            }
        }

        Iterable<String> allIndices = schema.indices();

        Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        Stream.ofAll(allIndices).forEach(index -> client.admin().indices()
                .create(new CreateIndexRequest(index)).actionGet());

        return Stream.ofAll(allIndices).count(s -> !s.isEmpty());
    }

    /**
     * drop all indices existing for this ontology
     * @param client
     * @param schema
     * @return
     */
    static int drop(Client client, RawSchema schema) {
        Iterable<String> indices = schema.indices();
        Stream.ofAll(indices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        return Stream.ofAll(indices).count(s -> !s.isEmpty());

    }


    /**
     *
     * @param zipIn
     * @return
     * @throws IOException
     */
    static ByteArrayOutputStream extractFile(InflaterInputStream zipIn) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(stream);
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
        return stream;
    }

    static Object parseValue(String explicitType, Object value, DateFormat sdf) {
        switch (explicitType) {
            case "text":
            case "string":
            case "stringValue":
                return value.toString();
            case "int":
            case "intValue":
                try {
                    return Integer.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    try {
                        return Long.valueOf(value.toString());
                    } catch (NumberFormatException e1) {
                        return value.toString();
                    }
                }
            case "long":
            case "longValue":
                try {
                    return Long.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    return value.toString();
                }
            case "float":
            case "floatValue":
                try {
                    return Float.valueOf(value.toString());
                } catch (NumberFormatException e) {
                    return value.toString();
                }
            case "date":
            case "dateValue":
                try {
                    return sdf.parse(value.toString());
                } catch (ParseException e) {
                    try {
                        return sdf.format(new Date(value.toString()));
                    } catch (Throwable e1) {
                        return value.toString();
                    }
                }
            case "geo":
            case "geoValue":
                return new Point(
                        Double.valueOf(value.toString().split("[,]")[1]),
                        Double.valueOf(value.toString().split("[,]")[0]));
        }
        return value;
    }

}
