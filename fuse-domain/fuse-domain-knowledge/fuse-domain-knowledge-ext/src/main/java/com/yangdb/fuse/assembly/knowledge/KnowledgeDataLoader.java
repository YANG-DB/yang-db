package com.yangdb.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.yangdb.fuse.assembly.knowledge.load.KnowledgeContext;
import com.yangdb.fuse.assembly.knowledge.load.KnowledgeTransformer;
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerProvider;
import com.yangdb.fuse.executor.ontology.schema.GraphDataLoader;
import com.yangdb.fuse.executor.ontology.schema.RawSchema;
import com.yangdb.fuse.model.logical.LogicalGraphModel;
import com.yangdb.fuse.model.ontology.transformer.OntologyTransformer;
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
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.TimeZone;

import static com.yangdb.fuse.assembly.knowledge.load.KnowledgeWriterContext.commit;

/**
 * Created by lior.perry on 2/11/2018.
 */
public class KnowledgeDataLoader implements GraphDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeDataLoader.class);

    private Client client;
    private SimpleDateFormat sdf;
    private RawSchema schema;
    private KnowledgeTransformer transformer;
    private ObjectMapper mapper;

    @Inject
    public KnowledgeDataLoader(Config config, Client client, RawSchema schema, OntologyTransformerProvider transformerProvider, KnowledgeIdGenerator idGenerator) {
        this.schema = schema;
        this.mapper = new ObjectMapper();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        //load knowledge transformer
        final Optional<OntologyTransformer> assembly = transformerProvider.transformer(config.getString("assembly"));
        if(!assembly.isPresent())
            throw new IllegalArgumentException("No transformer provider found for selected ontology "+config.getString("assembly"));
        this.transformer = new KnowledgeTransformer(assembly.get(),schema,idGenerator );
        this.client = client;
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
    public long load(LogicalGraphModel root) throws JsonProcessingException {
        final KnowledgeContext context = transformer.transform(root);
        //load all data to designated indices according to schema
        return commit(client,schema,mapper,context);
    }

    @Override
    public long load(File data) throws IOException {
        return 0;
    }
}
