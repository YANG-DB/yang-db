package com.kayhut.test.scenario;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.kayhut.test.framework.populator.DataPopulator;
import com.kayhut.test.framework.populator.ElasticDataPopulator;
import com.kayhut.test.framework.providers.FileCsvDataProvider;
import org.elasticsearch.client.transport.TransportClient;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by moti on 11/04/2017.
 */
public class DragonScenarioFolderElasticPopulator implements DataPopulator {
    private TransportClient client;
    private String dataFolder;
    private String indexName;

    public DragonScenarioFolderElasticPopulator(TransportClient client, String dataFolder, String indexName) {
        this.client = client;
        this.dataFolder = dataFolder;
        this.indexName = indexName;
    }

    @Override
    public void populate() throws IOException {
        loadFile(DragonScenarioConversionConstants.DRAGONS_FILE, DragonScenarioConversionConstants.DRAGON_FILE_SCHEMA, "dragon", "id");
        loadFile(DragonScenarioConversionConstants.HORSES_FILE, DragonScenarioConversionConstants.HORSE_FILE_SCHEMA, "horse", "id");
        loadFile(DragonScenarioConversionConstants.PERSON_FILE, DragonScenarioConversionConstants.PERSON_FILE_SCHEMA, "person", "id");
        loadFile(DragonScenarioConversionConstants.GUILD_FILE, DragonScenarioConversionConstants.GUILD_FILE_SCHEMA, "guild", "id");
        loadFile(DragonScenarioConversionConstants.KINGDOM_FILE, DragonScenarioConversionConstants.KINGDOM_FILE_SCHEMA, "kingdom", "id");
        loadFile(DragonScenarioConversionConstants.DRAGON_ORIGINATED_IN_FILE, DragonScenarioConversionConstants.DRAGON_ORIGINATED_IN_ELASTIC_SCHEMA, "originated_in", "");
        loadFile(DragonScenarioConversionConstants.DRAGON_FIRES_AT_FILE, DragonScenarioConversionConstants.DRAGON_FIRES_AT_ELASTIC_SCHEMA, "fires_at", "");
        loadFile(DragonScenarioConversionConstants.DRAGON_FREEZES_FILE, DragonScenarioConversionConstants.DRAGON_FREEZES_ELASTIC_SCHEMA, "freezes", "");
        loadFile(DragonScenarioConversionConstants.GUILD_REGISTERED_IN_FILE, DragonScenarioConversionConstants.GUILD_REGISTERED_IN_ELASTIC_SCHEMA, "registered_in", "");
        loadFile(DragonScenarioConversionConstants.HORSE_ORIGINATED_IN_FILE, DragonScenarioConversionConstants.HORSE_ORIGINATED_IN_ELASTIC_SCHEMA, "originated_in", "");
        loadFile(DragonScenarioConversionConstants.PERSON_KNOWS_FILE, DragonScenarioConversionConstants.PERSON_KNOWS_ELASTIC_SCHEMA, "knows", "");
        loadFile(DragonScenarioConversionConstants.PERSON_MEMBER_OF_FILE, DragonScenarioConversionConstants.PERSON_MEMBER_OF_ELASTIC_SCHEMA, "member_of", "");
        loadFile(DragonScenarioConversionConstants.PERSON_OFFSPRING_FILE, DragonScenarioConversionConstants.PERSON_OFFSPRING_ELASTIC_SCHEMA, "parent", "");
        loadFile(DragonScenarioConversionConstants.PERSON_OWNS_DRAGON_FILE, DragonScenarioConversionConstants.PERSON_OWNS_DRAGON_ELASTIC_SCHEMA, "owns", "");
        loadFile(DragonScenarioConversionConstants.PERSON_OWNS_HORSE_FILE, DragonScenarioConversionConstants.PERSON_OWNS_HORSE_ELASTIC_SCHEMA, "owns", "");
        loadFile(DragonScenarioConversionConstants.PERSON_SUBJECT_OF_FILE, DragonScenarioConversionConstants.PERSON_SUBJECT_OF_ELASTIC_SCHEMA, "subject_of", "");
    }

    private void loadFile(String fileName, CsvSchema schema, String type, String idField) throws IOException {
        FileCsvDataProvider csvDataProvider = new FileCsvDataProvider(Paths.get(dataFolder, fileName).toString(),schema);
        ElasticDataPopulator elasticDataPopulator = new ElasticDataPopulator(client, indexName, type, idField, csvDataProvider);
        elasticDataPopulator.populate();
    }
}
