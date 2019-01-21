package com.kayhut.fuse.assembly.knowledge.domain;

/*-
 * #%L
 * fuse-domain-knowledge-test
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

import com.cedarsoftware.util.io.JsonIoException;
import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import javaslang.collection.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplatesRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by rani on 5/21/2018.
 */
public class KnowledgeDatasetLoader {
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeDatasetLoader.class);

    private final String cIndexType = "pge";
    private final String cEntity = "entity";
    private final String cEntityValue = "e.value";
    private final String cInsight = "insight";

    private TransportClient client;
    private SimpleDateFormat sdf;
    private Config conf;
    private RawSchema schema;

    private ObjectMapper _mapper;

    private Map<String, Integer> _entitiesMap;
    private Map<Integer, ObjectNode> _elements;
    private Map<Integer, String> _elementsTypes;
    private int _entityId;
    private final String cContext = "context1";
    private final Integer cAuthCount = 1;
    private ArrayNode _fixedAuth;
    private SimpleDateFormat _releasedFormat;

    public KnowledgeDatasetLoader(String confPath) throws UnknownHostException {
        try {
            File configFile = new File(confPath);
            this.conf = ConfigFactory.parseFileAnySyntax(configFile, ConfigParseOptions.defaults().setAllowMissing(false));
            this.schema = ((Class<? extends RawSchema>) Class.forName(conf.getString(conf.getString("assembly") + ".physical_raw_schema"))).newInstance();

            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        } catch (Exception exc) {
            String exception = exc.getMessage();
        }
    }

    public void client_connect() {
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
    }

    public void client_close() {
        client.close();
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
                    client.admin().indices().deleteTemplate(new DeleteIndexTemplateRequest(templateName)).actionGet();
                }
                client.admin().indices().putTemplate(new PutIndexTemplateRequest(templateName).source(template, XContentType.JSON)).actionGet();
            }
        }

        Iterable<String> allIndices = schema.indices();

        Stream.ofAll(allIndices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        Stream.ofAll(allIndices).forEach(index -> client.admin().indices().create(new CreateIndexRequest(index)).actionGet());

        return Stream.ofAll(allIndices).count(s -> !s.isEmpty());
    }

    public long drop() throws IOException {
        Iterable<String> indices = schema.indices();
        Stream.ofAll(indices)
                .filter(index -> client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet().isExists())
                .forEach(index -> client.admin().indices().delete(new DeleteIndexRequest(index)).actionGet());
        return Stream.ofAll(indices).count(s -> !s.isEmpty());
    }

    private void createEntity(String category, String released) throws ParseException {
        ObjectNode movie = _mapper.createObjectNode();
        movie.put("type", cEntity);
        movie.put("logicalId", "e" + String.format(schema.getIdFormat("entity"), _entityId));
        movie.put("context", cContext);
        movie.put("category", category);
        movie.put("authorizationCount", cAuthCount);
        movie.put("lastUpdateUser", "imdb");
        movie.put("creationUser", "imdb");

        if (category.equals("movie")) {
            if (!released.equals("N/A")) {
                try {
                    Date releasedDate = _releasedFormat.parse(released);
                    movie.put("creationTime", sdf.format(releasedDate));
                    movie.put("lastUpdateTime", sdf.format(releasedDate));
                } catch (ParseException pe) {
                    int a=3;
                }
            } else {
                movie.put("creationTime", sdf.format(0));
                movie.put("lastUpdateTime", sdf.format(System.currentTimeMillis()));
            }
        } else {
            movie.put("creationTime", sdf.format(0));
            movie.put("lastUpdateTime", sdf.format(System.currentTimeMillis()));
        }

        movie.put("authorization", _fixedAuth);
        movie.put("id", _entityId);
        _elements.put(_entityId, movie);
        _elementsTypes.put(_entityId, "entity");

        //ArrayNode refsNode = _mapper.createArrayNode();
        //refsNode.add("ref" + String.format(schema.getIdFormat("reference"), 1));
        //on.put("refs", refsNode);
    }

    private boolean createEntityValue(String value, String valueName, int entityID) {
        if (value.equals("N/A"))
            return false;
        ObjectNode on = _mapper.createObjectNode();
        on.put("type", cEntityValue);
        on.put("logicalId", "e" + String.format(schema.getIdFormat("entity"), entityID));
        on.put("entityId", "e" + String.format(schema.getIdFormat("entity"), entityID) + "." + cContext);
        on.put("context", cContext);
        on.put("authorizationCount", cAuthCount);
        on.put("fieldId", valueName);
        on.put("bdt", valueName);
        switch (valueName) {
            case "year":
                on.put("intValue", Integer.parseInt(value.replaceAll("\\D+","")));
                break;
            case "runtime":
                on.put("intValue", Integer.parseInt(value.replaceAll("\\D+","")));
                break;
            case "metascore":
                try {
                    on.put("intValue", Integer.parseInt(value.replaceAll("\\D+", "")));
                } catch (NumberFormatException nfe) {
                    String abc = "";
                }
                break;
            case "imdbVotes":
                on.put("intValue", Integer.parseInt(value.replaceAll("\\D+","")));
                break;
            case "imdbRating":
                try {
                    on.put("floatValue", Float.parseFloat(value));
                } catch (NumberFormatException nfe) {
                    String newVal = value.replaceAll("(^\\d+\\.\\d+).*", "$1");
                    int i=3;
                }
                break;
            case "rated":
                on.put("stringValue", value);
                break;
            case "genre":
                on.put("stringValue", value);
                break;
            case "language":
                on.put("stringValue", value);
                break;
            case "response":
                on.put("stringValue", value);
                break;
            case "country":
                on.put("stringValue", value);
                break;
            case "imdbID":
                on.put("stringValue", value);
                break;
            default:
                on.put("stringValue", value);
                break;
        }
        on.put("refs", "[]");
        on.put("lastUpdateUser", "imdb");
        on.put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())));
        on.put("creationUser", "imdb");
        on.put("creationTime", sdf.format(new Date(System.currentTimeMillis())));
        on.put("id", _entityId);

        _elements.put(_entityId, on);
        _elementsTypes.put(_entityId, "e.value");
        return true;
    }

    private void addMovieEntityAndValues(String movie, String year, String released, String rated, String genre, String runtime, String metascore, String language,
                                         String country, String imdbRating, String imdbVotes, String imdbID, String response) throws ParseException {
        int movieID = _entityId;
        _entitiesMap.put(movie, _entityId);
        createEntity("movie", released);
        _entityId++;

        if (createEntityValue(movie, "title", movieID))
            _entityId++;
        if (createEntityValue(year, "year", movieID))
            _entityId++;
        if (createEntityValue(rated, "rated", movieID))
            _entityId++;
        if (createEntityValue(genre, "genre", movieID))
            _entityId++;
        if (createEntityValue(runtime, "runtime", movieID))
            _entityId++;
        if (createEntityValue(metascore, "metascore", movieID))
            _entityId++;
        if (createEntityValue(language, "language", movieID))
            _entityId++;
        if (createEntityValue(country, "country", movieID))
            _entityId++;
        if (createEntityValue(imdbRating, "imdbRating", movieID))
            _entityId++;
        if (createEntityValue(imdbVotes, "imdbVotes", movieID))
            _entityId++;
        if (createEntityValue(imdbID, "imdbID", movieID))
            _entityId++;
        if (createEntityValue(response, "response", movieID))
            _entityId++;
    }

    private void addRelation(String category, String entityA, String aCategory, String entityB, String bCategory) {
        ObjectNode on = _mapper.createObjectNode();
        on.put("type", "relation");
        on.put("entityAId", "e" + String.format(schema.getIdFormat("entity"), _entitiesMap.get(entityA)) + "." + cContext);
        on.put("entityACategory", aCategory);
        on.put("entityALogicalId", "e" + String.format(schema.getIdFormat("entity"), _entitiesMap.get(entityA)));
        Integer entityBid = 0;
        switch (bCategory) {
            case "director":
                entityBid = _entitiesMap.get(entityB+"-D");
                break;
            case "actor":
                entityBid = _entitiesMap.get(entityB+"-A");
                break;
            case "writer":
                entityBid = _entitiesMap.get(entityB+"-W");
                break;
        }
        on.put("entityBId", "e" + String.format(schema.getIdFormat("entity"), entityBid) + "." + cContext);
        on.put("entityBCategory", bCategory);
        on.put("entityBLogicalId", "e" + String.format(schema.getIdFormat("entity"), entityBid));
        on.put("context", cContext);
        on.put("category", category);
        on.put("authorization", _fixedAuth);
        on.put("authorizationCount", cAuthCount);
        on.put("refs", "[]");
        on.put("creationUser", "imdb");
        on.put("lastUpdateUser", "imdb");
        on.put("creationTime", sdf.format(0));
        on.put("lastUpdateTime", sdf.format(System.currentTimeMillis()));
        on.put("id", _entityId);

        _elements.put(_entityId, on);
        _elementsTypes.put(_entityId, "relation");
    }

    private void addMovieDirector(String director, String movieTitle) throws ParseException {
        if (director.equals("N/A"))
            return;
        // Check for existence of director, if is exists, add relation only
        if (!_entitiesMap.containsKey(director + "-D")) {
            // Adding director entity
            _entitiesMap.put(director + "-D", _entityId);
            createEntity("director", "");
            int directorID = _entityId;
            _entityId++;
            // Adding director name as entity
            if (createEntityValue(director, "director", directorID))
                _entityId++;
        }
        // Adding the relation
        addRelation("directedBy", movieTitle, "movie", director, "director");
        _entityId++;
    }

    private void addRelationValue(int relationID, String value) {
        ObjectNode on = _mapper.createObjectNode();
        on.put("type", "r.value");
        on.put("relationId", "r" + String.format(schema.getIdFormat("relation"), relationID));
        on.put("context", cContext);
        on.put("authorization", _fixedAuth);
        on.put("authorizationCount", cAuthCount);
        on.put("fieldId", "type");
        on.put("bdt", "type");
        on.put("stringValue", value);
        on.put("refs", "[]");
        on.put("creationUser", "imdb");
        on.put("lastUpdateUser", "imdb");
        on.put("creationTime", sdf.format(0));
        on.put("lastUpdateTime", sdf.format(System.currentTimeMillis()));
        on.put("id", _entityId);

        _elements.put(_entityId, on);
        _elementsTypes.put(_entityId, "r.value");
    }

    private void addMovieWriters(String writers, String movieTitle) throws ParseException {
        if (writers.equals("N/A"))
            return;
        String[] writersArr = writers.split(",");

        for(String writer:writersArr) {
            // Dealing with writer type
            int typeIdx = writer.indexOf('(');
            String type = null;
            String writerOnly = null;
            try {
                if (typeIdx >= 0) {
                    int endTypeIdx = writer.indexOf(')');
                    if (endTypeIdx >= 0)
                        type = writer.substring(typeIdx + 1, endTypeIdx);
                    else type = writer.substring(typeIdx + 1);
                    writerOnly = writer.substring(0, typeIdx );
                }
            } catch (StringIndexOutOfBoundsException sie) {
                int x=3;
            }

            if (writerOnly == null) {
                writerOnly = writer;
            }

            // Add writer only if not existing already
            if (!_entitiesMap.containsKey(writerOnly + "-W")) {
                // Adding director entity
                _entitiesMap.put(writerOnly + "-W", _entityId);
                createEntity("writer", "");
                int writerID = _entityId;
                _entityId++;
                // Adding director name as entity
                if (createEntityValue(writerOnly, "writer", writerID))
                    _entityId++;
            }
            // Adding the relation & type (if exists)
            addRelation("writtenBy", movieTitle, "movie", writerOnly, "writer");
            int relationID = _entityId;
            _entityId++;
            if (type != null) {
                addRelationValue(relationID, type);
                _entityId++;
            }
        }
    }

    private void addMovieActors(String actors, String movieTitle) throws ParseException {
        if (actors.equals("N/A"))
            return;
        String[] actorsArr = actors.split(",");
        for(String actor:actorsArr) {
            // check if actor already exists. If it does, no need to insert entity & entity-value, just add the relation
            if (!_entitiesMap.containsKey(actor+"-A")) {
                // Adding actor entity
                _entitiesMap.put(actor + "-A", _entityId);
                createEntity("actor","");
                int actorID = _entityId;
                _entityId++;
                // Adding actor name as entity
                if (createEntityValue(actor, "actor", actorID))
                    _entityId++;
            }
            // Adding the relation
            addRelation("acting", movieTitle, "movie", actor, "actor");
            _entityId++;
        }
    }

    private void addMovieReferences(String plot, String poster, String title) {
        boolean hasPlot = false;
        boolean hasPoster = false;
        ObjectNode on = _mapper.createObjectNode();
        on.put("authorization", _fixedAuth);
        on.put("authorizationCount", cAuthCount);
        on.put("system", "system");
        on.put("type", "reference");
        on.put("creationUser", "imdb");
        on.put("lastUpdateUser", "imdb");
        on.put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())));
        on.put("creationTime", sdf.format(0));
        if (!poster.equals("N/A")) {
            on.put("url", poster);
            hasPlot = true;
        }
        on.put("title", "Plot of " + title);
        if (!plot.equals("N/A")) {
            on.put("value", plot);
            hasPlot = true;
        }

        if (!hasPlot && !hasPlot)
            return;
        on.put("id", _entityId);

        _elements.put(_entityId, on);
        _elementsTypes.put(_entityId, "reference");

        // update the movie entity to contain the reference
        ObjectNode movieObject = _elements.get(_entitiesMap.get(title));
        ArrayNode refNode = _mapper.createArrayNode();
        refNode.add("ref" + String.format(schema.getIdFormat("reference"), _entityId));
        if (movieObject == null) {
            int x=3;
        }
        movieObject.put("refs", refNode);

        _entityId++;
    }

    private void addMovieInsight(String awards, String title) {
        if (awards.equals("N/A"))
            return;

        ObjectNode on = _mapper.createObjectNode();
        on.put("type", "insight");
        on.put("content", awards);
        on.put("context", cContext);
        on.put("entityIds", "e" + String.format(schema.getIdFormat("entity"), _entitiesMap.get(title)) + "." + cContext);
        on.put("refs", "[]");
        on.put("authorization", _fixedAuth);
        on.put("authorizationCount", cAuthCount);
        on.put("creationUser", "imdb");
        on.put("lastUpdateUser", "imdb");
        on.put("lastUpdateTime", sdf.format(new Date(System.currentTimeMillis())));
        on.put("creationTime", sdf.format(0));
        on.put("id", _entityId);

        _elements.put(_entityId, on);
        _elementsTypes.put(_entityId, "insight");

        ObjectNode ie = _mapper.createObjectNode();
        ie.put("type", "e.insight");
        ie.put("entityId", "e" + String.format(schema.getIdFormat("entity"), _entitiesMap.get(title)) + "." + cContext);
        ie.put("insightId", "i" + String.format(schema.getIdFormat("insight"), _entityId));
        _entityId++;
        ie.put("id", _entityId);

        _elements.put(_entityId, ie);
        _elementsTypes.put(_entityId, "e.insight");

        _entityId++;
    }

    public void loadFromIMDBJson(int rows_count) throws FileNotFoundException, IOException, ParseException {
        String currJsonLine = null;
        _entitiesMap = new LinkedHashMap<String, Integer>();
        _elements = new LinkedHashMap<Integer, ObjectNode>();
        _elementsTypes = new LinkedHashMap<Integer, String>();
        _mapper = new ObjectMapper();

        // Setting fixed authorizations
        _fixedAuth = _mapper.createArrayNode();
        _fixedAuth.add("source1.procedure1");
        _fixedAuth.add("source2.procedure2");

        // Some more setting
        _releasedFormat = new SimpleDateFormat("dd MMM yyyy");

        // Open the file
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String datasetFilepath = classLoader.getResource("assembly/Knowledge/dataset/film.imdb.json").getFile();
        FileInputStream fstream = new FileInputStream(datasetFilepath);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        String strLine;
        _entityId = 0;
        int line_counter = 0;

        //Read File Line By Line
        while ((strLine = br.readLine()) != null) {
            // skip, if line is empty in the middle
            if (strLine.length() == 0)
                continue;
            // Print the content on the console
            JsonObject<String, String> read = null;
            try {
                read = (JsonObject<String, String>) JsonReader.jsonToJava(strLine);
            } catch (JsonIoException jie) {
                // ignore issues due to problem in file
                continue;
            }
            // Ignoring the "Response":"false" json lines
            if (read.containsKey("Error")) {
                continue;
            }
            // Dealing with a single line which contains film's data
            String title = read.get("Title");
            addMovieEntityAndValues(title, read.get("Year"), read.get("Released"), read.get("Rated"), read.get("Genre"), read.get("Runtime"), read.get("Metascore"),
                    read.get("Language"), read.get("Country"), read.get("imdbRating"), read.get("imdbVotes"), read.get("imdbID"), read.get("Response"));
            addMovieDirector(read.get("Director"), title);
            addMovieWriters(read.get("Writer"), title);
            addMovieActors(read.get("Actors"), title);

            addMovieReferences(read.get("Plot"), read.get("Poster"), title);
            addMovieInsight(read.get("Awards"), title);
            line_counter++;
            if (rows_count > 0) {
                if (line_counter == rows_count)
                    break;
            }
            if (line_counter % 3000 == 0) {
                System.out.println("Indexing after " + line_counter + " elements.");
                indexImdbJsons();
                System.out.println("Finished Indexing last bulk");
            }
        }

        //Close the input stream
        br.close();
    }

    // Adding the .in & .out relations to the entity index
    private void addRelationAdditionals(String relationId, Integer idx, BulkRequestBuilder bulk) throws JsonProcessingException {
        //String aLogicalId = "e" + String.format(schema.getIdFormat("entity"), _elements.get(idx).get("entityALogicalId"));
        String aLogicalId = _elements.get(idx).get("entityALogicalId").asText();
        String aIndex = Stream.ofAll(schema.getPartitions("entity")).map(partition -> (IndexPartitions.Partition.Range) partition)
                .filter(partition -> partition.isWithin(aLogicalId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

        //String bLogicalId = "e" + String.format(schema.getIdFormat("entity"), _elements.get(idx).get("entityBLogicalId"));
        String bLogicalId = _elements.get(idx).get("entityBLogicalId").asText();
        String bIndex = Stream.ofAll(schema.getPartitions("entity")).map(partition -> (IndexPartitions.Partition.Range) partition)
                .filter(partition -> partition.isWithin(bLogicalId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);

        ObjectNode on = _mapper.createObjectNode();
        on.put("type", "e.relation");
        on.put("entityAId", _elements.get(idx).get("entityAId").asText());
        on.put("entityACategory", _elements.get(idx).get("entityACategory").asText());
        on.put("entityBId", _elements.get(idx).get("entityBId").asText());
        on.put("entityBCategory", _elements.get(idx).get("entityBCategory").asText());
        on.put("relationId", relationId);
        on.put("direction", "out");
        on.put("context", _elements.get(idx).get("context").asText());
        on.put("category", _elements.get(idx).get("category").asText());
        on.put("authorization", _elements.get(idx).get("authorization").asText());
        on.put("authorizationCount", cAuthCount);
        on.put("lastUpdateUser", _elements.get(idx).get("lastUpdateUser").asText());
        on.put("lastUpdateTime", _elements.get(idx).get("lastUpdateTime").asText());
        on.put("creationUser", _elements.get(idx).get("creationUser").asText());
        on.put("creationTime", _elements.get(idx).get("creationTime").asText());
        bulk.add(client.prepareIndex().setIndex(aIndex).setType(cIndexType).setId(relationId + ".out")
                .setOpType(IndexRequest.OpType.INDEX).setRouting(aLogicalId)
                .setSource(_mapper.writeValueAsString(on), XContentType.JSON));

        on = _mapper.createObjectNode();
        on.put("type", "e.relation");
        on.put("entityAId", _elements.get(idx).get("entityBId").asText());
        on.put("entityACategory", _elements.get(idx).get("entityBCategory").asText());
        on.put("entityBId", _elements.get(idx).get("entityAId").asText());
        on.put("entityBCategory", _elements.get(idx).get("entityACategory").asText());
        on.put("relationId", relationId);
        on.put("direction", "in");
        on.put("context", _elements.get(idx).get("context").asText());
        on.put("category", _elements.get(idx).get("category").asText());
        on.put("authorization", _elements.get(idx).get("authorization").asText());
        on.put("authorizationCount", cAuthCount);
        on.put("lastUpdateUser", _elements.get(idx).get("lastUpdateUser").asText());
        on.put("lastUpdateTime", _elements.get(idx).get("lastUpdateTime").asText());
        on.put("creationUser", _elements.get(idx).get("creationUser").asText());
        on.put("creationTime", _elements.get(idx).get("creationTime").asText());
        bulk.add(client.prepareIndex().setIndex(bIndex).setType(cIndexType).setId(relationId + ".in")
                .setOpType(IndexRequest.OpType.INDEX).setRouting(bLogicalId)
                .setSource(_mapper.writeValueAsString(on), XContentType.JSON));
    }

    public void indexImdbJsons() throws JsonProcessingException
    {
        long start = System.currentTimeMillis();

        BulkRequestBuilder bulk = client.prepareBulk();

        String currIndexId = "-3";
        String currIndex = null;
        String logicalId = null;
        boolean setRouting = false;

        for(Integer i:_elementsTypes.keySet()) {
            try {
                currIndexId = _elements.get(i).get("id").asText();
            } catch (NullPointerException npe) {
                int kk = 4;
            }
            switch (_elementsTypes.get(i)) {
                case "entity" :
                    currIndexId = "e" + String.format(schema.getIdFormat("entity"), Integer.parseInt(currIndexId)) + "." + cContext;
                    String id = currIndexId;
                    currIndex = Stream.ofAll(schema.getPartitions("entity")).map(partition -> (IndexPartitions.Partition.Range) partition)
                            .filter(partition -> partition.isWithin(id)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
                    break;
                case "e.value" :
                    currIndexId = "ev" + currIndexId;
                    logicalId = _elements.get(i).get("logicalId").asText();
                    id = logicalId;
                    currIndex = Stream.ofAll(schema.getPartitions("entity")).map(partition -> (IndexPartitions.Partition.Range) partition)
                            .filter(partition -> partition.isWithin(id)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
                    setRouting = true;
                    break;
                case "reference" :
                    try {
                    currIndexId = "ref" + String.format(schema.getIdFormat("reference"), Integer.parseInt(currIndexId));
                    id = currIndexId;
                        currIndex = Stream.ofAll(schema.getPartitions("reference")).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                                .filter(partition -> partition.isWithin(id)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
                    } catch (Exception exc) {
                        int ll=3;
                    }
                    break;
                case "insight" :
                    currIndexId = "i" + String.format(schema.getIdFormat("insight"), Integer.parseInt(currIndexId));
                    id = currIndexId;
                    currIndex = Stream.ofAll(schema.getPartitions(cInsight)).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                            .filter(partition -> partition.isWithin(id)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
                    break;
                case "e.insight":
                    String entityId = _elements.get(i).get("entityId").asText();
                    logicalId = entityId.substring(0, entityId.indexOf('.'));
                    id = logicalId;
                    currIndex = Stream.ofAll(schema.getPartitions("entity")).map(partition -> (IndexPartitions.Partition.Range<String>) partition)
                            .filter(partition -> partition.isWithin(id)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
                    currIndexId = logicalId + "." + _elements.get(i).get("insightId").asText();
                    setRouting = true;
                    break;
                case "relation":
                    currIndexId = "r" + String.format(schema.getIdFormat("relation"), Integer.parseInt(currIndexId));
                    id = currIndexId;
                    currIndex = Stream.ofAll(schema.getPartitions("relation")).map(partition -> (IndexPartitions.Partition.Range) partition)
                            .filter(partition -> partition.isWithin(id)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
                    addRelationAdditionals(currIndexId, i, bulk);
                    break;
                case "r.value" :
                    String relationId = _elements.get(i).get("relationId").asText();
                    logicalId = relationId;
                    currIndexId = "rv" + currIndexId;
                    currIndex = Stream.ofAll(schema.getPartitions("relation")).map(partition -> (IndexPartitions.Partition.Range) partition)
                            .filter(partition -> partition.isWithin(relationId)).map(partition -> Stream.ofAll(partition.getIndices()).get(0)).get(0);
                    setRouting = true;
                    break;
                default:
                    break;
            }
            // remove the  id element ... as we don't need to index it.
            _elements.get(i).remove("id");
            if (setRouting) {
                bulk.add(client.prepareIndex().setIndex(currIndex).setType(cIndexType).setId(currIndexId)
                        .setOpType(IndexRequest.OpType.INDEX).setRouting(logicalId)
                        .setSource(_mapper.writeValueAsString(_elements.get(i)), XContentType.JSON));
            } else {
                bulk.add(client.prepareIndex().setIndex(currIndex).setType(cIndexType).setId(currIndexId)
                        .setOpType(IndexRequest.OpType.INDEX)
                        .setSource(_mapper.writeValueAsString(_elements.get(i)), XContentType.JSON));
            }
        }

        long middle1 = System.currentTimeMillis();
        System.out.println("Cleaning elements structures, before : " + _elements.size() + " in : " + (middle1-start));

        _elements.clear();
        _elementsTypes.clear();

        long middle2 = System.currentTimeMillis();

        System.out.println("after : " + _elements.size() + " , ... execution bulk in : " + (middle2-middle1));

        bulk.execute();

        long finish = System.currentTimeMillis();

        System.out.println("Bulk executed, in : " + (finish-middle2));
    }

}
