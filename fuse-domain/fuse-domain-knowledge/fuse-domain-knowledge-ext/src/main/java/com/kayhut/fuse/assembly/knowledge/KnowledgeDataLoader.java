package com.kayhut.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.kayhut.fuse.assembly.knowledge.load.KnowledgeContext;
import com.kayhut.fuse.assembly.knowledge.load.KnowledgeTransformer;
import com.kayhut.fuse.assembly.knowledge.load.KnowledgeWriterContext;
import com.kayhut.fuse.executor.ontology.schema.GraphDataLoader;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.typesafe.config.Config;
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
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.kayhut.fuse.assembly.knowledge.load.KnowledgeWriterContext.*;

/**
 * Created by lior.perry on 2/11/2018.
 */
public class KnowledgeDataLoader implements GraphDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeDataLoader.class);

    private TransportClient client;
    private SimpleDateFormat sdf;
    private Config conf;
    private RawSchema schema;
    private KnowledgeTransformer transformer;

    @Inject
    public KnowledgeDataLoader(Config conf, RawSchema schema) throws UnknownHostException {
        this.conf = conf;
        this.schema = schema;
        Settings settings = Settings.builder().put("cluster.name", conf.getConfig("elasticsearch").getString("cluster_name")).build();
        int port = conf.getConfig("elasticsearch").getInt("port");
        client = new PreBuiltTransportClient(settings);
        conf.getConfig("elasticsearch").getList("hosts").unwrapped().forEach(host -> {
            try {
                client.addTransportAddress(new TransportAddress(InetAddress.getByName(host.toString()), port));
            } catch (UnknownHostException e) {
                logger.error(e.getMessage(), e);
            }
        });
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        transformer = new KnowledgeTransformer();
    }


    public long init() throws IOException {
        String workingDir = System.getProperty("user.dir");
        File templates = Paths.get(workingDir, "indexTemplates").toFile();
        File[] templateFiles = templates.listFiles();
        if (templateFiles != null) {
            for (File templateFile : templateFiles) {
                String templateName = FilenameUtils.getBaseName(templateFile.getName());
                String template = FileUtils.readFileToString(templateFile, "utf-8");
                if (!client.admin().indices().getTemplates(new GetIndexTemplatesRequest(templateName)).actionGet().getIndexTemplates().isEmpty()) {
                    final AcknowledgedResponse acknowledgedResponse = client.admin().indices().deleteTemplate(new DeleteIndexTemplateRequest(templateName)).actionGet(1500);
                    if(!acknowledgedResponse.isAcknowledged()) return -1;
                }
                final AcknowledgedResponse acknowledgedResponse = client.admin().indices().putTemplate(new PutIndexTemplateRequest(templateName).source(template, XContentType.JSON)).actionGet(1500);
                if(!acknowledgedResponse.isAcknowledged()) return -1;
            }
        }

        Iterable<String> allIndices = schema.indices();

        Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        Stream.ofAll(allIndices).forEach(index -> client.admin().indices().create(new CreateIndexRequest(index)).actionGet());

        return Stream.ofAll(allIndices).count(s -> !s.isEmpty());
    }

    @Override
    public long drop() throws IOException {
        Iterable<String> indices = schema.indices();
        Stream.ofAll(indices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        return Stream.ofAll(indices).count(s -> !s.isEmpty());
    }

    /**
     * transform json graph and load all data to designated indices according to schema
     * @param root graph document
     * @return
     */
    public long load(JsonNode root) {
        final KnowledgeContext context = transformer.transform(schema, root);
        final KnowledgeWriterContext writerContext = new KnowledgeWriterContext(context);
        //todo load all data to designated indices according to schema
//        commit(client,w)
        return 0;
    }
}
