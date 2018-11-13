package com.kayhut.test.scenario;

/*-
 * #%L
 * fuse-domain-dragons-test
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

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by moti on 06/04/2017.
 */
public class DragonElasticFileConverter {




    public static void main(String args[]) throws IOException {
        if(args.length < 2){
            System.out.println("Please provide endA files folder and endB folder");
            return;
        }

        String srcFolder = args[0];
        String destFolder = args[1];

        IdGenerator idGenerator = new IdGenerator();

        Map<Integer, Integer> dragonIds = convertNodeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.DRAGONS_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.DRAGONS_FILE).toString(),
                idGenerator,
                "id",
                DragonScenarioConversionConstants.DRAGON_FILE_SCHEMA);

        Map<Integer, Integer> personIds = convertNodeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.PERSON_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.PERSON_FILE).toString(),
                idGenerator,
                "id",
                DragonScenarioConversionConstants.PERSON_FILE_SCHEMA);

        Map<Integer, Integer> horsesIds = convertNodeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.HORSES_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.HORSES_FILE).toString(),
                idGenerator,
                "id",
                DragonScenarioConversionConstants.HORSE_FILE_SCHEMA);
        Map<Integer, Integer> kingdomIds = convertNodeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.KINGDOM_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.KINGDOM_FILE).toString(),
                idGenerator,
                "id",
                DragonScenarioConversionConstants.KINGDOM_FILE_SCHEMA);

        Map<Integer, Integer> guildIds = convertNodeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.GUILD_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.GUILD_FILE).toString(),
                idGenerator,
                "id",
                DragonScenarioConversionConstants.GUILD_FILE_SCHEMA);

        convertEdgeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.PERSON_OWNS_HORSE_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.PERSON_OWNS_HORSE_FILE).toString(),
                personIds, horsesIds, "person_id", "horse_id", DragonScenarioConversionConstants.PERSON_OWNS_HORSE_SCHEMA,
                DragonScenarioConversionConstants.PERSON_OWNS_HORSE_ELASTIC_SCHEMA);

        convertEdgeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.PERSON_OWNS_DRAGON_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.PERSON_OWNS_DRAGON_FILE).toString(),
                personIds, horsesIds, "person_id", "dragon_id", DragonScenarioConversionConstants.PERSON_OWNS_DRAGON_SCHEMA,
                DragonScenarioConversionConstants.PERSON_OWNS_DRAGON_ELASTIC_SCHEMA);

        convertEdgeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.DRAGON_FIRES_AT_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.DRAGON_FIRES_AT_FILE).toString(),dragonIds,
                dragonIds, "dragon1_id", "dragon2_id",DragonScenarioConversionConstants.DRAGON_FIRES_AT_SCHEMA,
                DragonScenarioConversionConstants.DRAGON_FIRES_AT_ELASTIC_SCHEMA);

        convertEdgeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.DRAGON_FREEZES_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.DRAGON_FREEZES_FILE).toString(),dragonIds,
                dragonIds, "dragon1_id", "dragon2_id",DragonScenarioConversionConstants.DRAGON_FREEZES_SCHEMA,
                DragonScenarioConversionConstants.DRAGON_FREEZES_ELASTIC_SCHEMA);

        convertEdgeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.PERSON_OFFSPRING_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.PERSON_OFFSPRING_FILE).toString(),personIds,
                personIds, "parent_id", "child_id",DragonScenarioConversionConstants.PERSON_OFFSPRING_SCHEMA,
                DragonScenarioConversionConstants.PERSON_OFFSPRING_ELASTIC_SCHEMA);

        convertEdgeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.PERSON_KNOWS_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.PERSON_KNOWS_FILE).toString(),personIds,
                personIds, "person1_id", "person2_id",DragonScenarioConversionConstants.PERSON_KNOWS_SCHEMA,
                DragonScenarioConversionConstants.PERSON_KNOWS_ELASTIC_SCHEMA);

        convertEdgeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.PERSON_MEMBER_OF_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.PERSON_MEMBER_OF_FILE).toString(),personIds,
                guildIds, "person_id", "guild_id",DragonScenarioConversionConstants.PERSON_MEMBER_OF_SCHEMA,
                DragonScenarioConversionConstants.PERSON_MEMBER_OF_ELASTIC_SCHEMA);

        convertEdgeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.PERSON_SUBJECT_OF_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.PERSON_SUBJECT_OF_FILE).toString(),personIds,
                kingdomIds, "person_id", "kingdom_id",DragonScenarioConversionConstants.PERSON_SUBJECT_OF_SCHEMA,
                DragonScenarioConversionConstants.PERSON_SUBJECT_OF_ELASTIC_SCHEMA);

        convertEdgeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.GUILD_REGISTERED_IN_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.GUILD_REGISTERED_IN_FILE).toString(),guildIds,
                kingdomIds, "guild_id", "kingdom_id",DragonScenarioConversionConstants.GUILD_REGISTERED_IN_SCHEMA,
                DragonScenarioConversionConstants.GUILD_REGISTERED_IN_ELASTIC_SCHEMA);

        convertEdgeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.HORSE_ORIGINATED_IN_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.HORSE_ORIGINATED_IN_FILE).toString(),horsesIds,
                kingdomIds, "horse_id", "kingdom_id",DragonScenarioConversionConstants.HORSE_ORIGINATED_IN_SCHEMA,
                DragonScenarioConversionConstants.HORSE_ORIGINATED_IN_ELASTIC_SCHEMA);

        convertEdgeFile(Paths.get(srcFolder, DragonScenarioConversionConstants.DRAGON_ORIGINATED_IN_FILE).toString(),
                Paths.get(destFolder, DragonScenarioConversionConstants.DRAGON_ORIGINATED_IN_FILE).toString(),dragonIds,
                kingdomIds, "dragon_id", "kingdom_id",DragonScenarioConversionConstants.DRAGON_ORIGINATED_IN_SCHEMA,
                DragonScenarioConversionConstants.DRAGON_ORIGINATED_IN_ELASTIC_SCHEMA);
    }

    private static Map<Integer, Integer> convertNodeFile(String srcFile, String destFile, IdGenerator idGenerator, String idField, CsvSchema schema) throws IOException {
        CsvMapper mapper = new CsvMapper();

        ObjectReader reader = mapper.readerFor(Map.class).with(schema);
        MappingIterator<HashMap<String, Object>> objectMappingIterator = reader.readValues(new File(srcFile));
        Map<Integer, Integer> idMap = new HashMap<>();
        CsvMapper outMapper = new CsvMapper();
        ObjectWriter writer = outMapper.writerFor(Map.class).with(schema);
        try(SequenceWriter sequenceWriter = writer.writeValues(new File(destFile))) {

            for (; objectMappingIterator.hasNext(); ) {
                HashMap<String, Object> next = objectMappingIterator.next();
                int newId = idGenerator.getNextId();
                idMap.put(Integer.parseInt((String)next.get(idField)), newId);
                next.put(idField, newId);
                sequenceWriter.write(next);
            }
        }
        return idMap;
    }

    private static void convertEdgeFile(String srcFile, String destFile,
                                        Map<Integer, Integer> fromIdMap,
                                        Map<Integer, Integer> toIdMap,
                                        String fromId, String toId,
                                        CsvSchema fileSchema, CsvSchema outSchema) throws IOException {
        CsvMapper mapper = new CsvMapper();

        ObjectReader reader = mapper.readerFor(Map.class).with(fileSchema);
        MappingIterator<HashMap<String, Object>> objectMappingIterator = reader.readValues(new File(srcFile));
        CsvMapper outMapper = new CsvMapper();

        ObjectWriter writer = outMapper.writerFor(Map.class).with(outSchema);
        try(SequenceWriter sequenceWriter = writer.writeValues(new File(destFile))) {

            for (; objectMappingIterator.hasNext(); ) {
                HashMap<String, Object> next = objectMappingIterator.next();
                HashMap<String, Object> edgeData = new HashMap<>(next);
                for(String field : next.keySet()){
                    if(outSchema.column(field) == null){
                        edgeData.remove(field);
                    }
                }
                int entityAId = Integer.parseInt((String)next.get(fromId));
                int entityBId = Integer.parseInt((String)next.get(toId));
                edgeData.put(DragonScenarioConversionConstants.SIDE_A_ID, fromIdMap.get(entityAId));
                edgeData.put(DragonScenarioConversionConstants.SIDE_B_ID, toIdMap.get(entityBId));
                edgeData.put(DragonScenarioConversionConstants.DIRECTION_COLUMN, DragonScenarioConversionConstants.OUT_DIRECTION);
                sequenceWriter.write(edgeData);
                edgeData = new HashMap<>(next);
                for(String field : next.keySet()){
                    if(outSchema.column(field) == null){
                        edgeData.remove(field);
                    }
                }

                edgeData.put(DragonScenarioConversionConstants.SIDE_B_ID, fromIdMap.get(entityAId));
                edgeData.put(DragonScenarioConversionConstants.SIDE_A_ID, toIdMap.get(entityBId));
                edgeData.put(DragonScenarioConversionConstants.DIRECTION_COLUMN, DragonScenarioConversionConstants.IN_DIRECTION);
                sequenceWriter.write(edgeData);
            }
        }
    }


    private static class IdGenerator{
        private int currentId = 0;

        public int getNextId(){
            return ++currentId;
        }
    }
}
