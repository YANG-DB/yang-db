package com.yangdb.fuse.executor.ontology.schema.load;

/*-
 * #%L
 * fuse-dv-core
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

import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import javaslang.collection.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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
}
