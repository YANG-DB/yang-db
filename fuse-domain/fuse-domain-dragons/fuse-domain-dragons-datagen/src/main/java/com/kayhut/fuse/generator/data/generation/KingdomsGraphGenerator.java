package com.kayhut.fuse.generator.data.generation;

/*-
 * #%L
 * fuse-domain-gragons-datagen
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

import com.kayhut.fuse.generator.configuration.KingdomConfiguration;
import com.kayhut.fuse.generator.data.generation.entity.KingdomGenerator;
import com.kayhut.fuse.generator.model.entity.EntityBase;
import com.kayhut.fuse.generator.model.entity.Kingdom;
import com.kayhut.fuse.generator.model.enums.EntityType;
import com.kayhut.fuse.generator.model.enums.RelationType;
import com.kayhut.fuse.generator.model.relation.Originated;
import com.kayhut.fuse.generator.model.relation.Registered;
import com.kayhut.fuse.generator.model.relation.RelationBase;
import com.kayhut.fuse.generator.model.relation.SubjectOf;
import com.kayhut.fuse.generator.util.CsvUtil;
import com.kayhut.fuse.generator.util.RandomUtil;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by benishue on 05/06/2017.
 */
public class KingdomsGraphGenerator {

    private static final Logger logger = LoggerFactory.getLogger(KingdomsGraphGenerator.class);
    //Used to skew the results
    private static final double LARGEST_KINGDOM_RATIO = 0.3;

    public KingdomsGraphGenerator(final KingdomConfiguration configuration) {
        this.kingdomConf = configuration;
    }

    public List<String> generateKingdomsGraph() {
        return Stream.ofAll(generateKingdoms()).map(EntityBase::getId).toJavaList();
    }

    public List<Kingdom> generateKingdoms() {
        List<Kingdom> kingdomsList = new ArrayList<>();
        List<String[]> kingdomsRecords = new ArrayList<>();
        try {
            KingdomGenerator generator = new KingdomGenerator(kingdomConf);

            int kingdomsSize = kingdomConf.getNumberOfNodes();
            String[] kingdomsNames = kingdomConf.getKingdoms();

            for (int i = 0; i < kingdomsSize; i++) {
                Kingdom kingdom = generator.generate();
                kingdom.setName(kingdomsNames[i]);
                kingdom.setId(Integer.toString(i));
                kingdomsList.add(kingdom);
                kingdomsRecords.add(kingdom.getRecord());
            }

            //write kingdoms
            CsvUtil.appendResults(kingdomsRecords, kingdomConf.getEntitiesFilePath());
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
        return kingdomsList;
    }

    /**
     * @param kingdomsIdList 0...Number of kingdoms
     * @param personsIdList  0...Number of persons
     * @return EdgeSet ( List of Tuple<Person Id, Kingdom ID>) of relationships between Persons and Kingdoms.
     */
    public List<Tuple2> attachPersonToKingdom(List<String> kingdomsIdList, List<String> personsIdList) {
        List<Tuple2> entityToKingdom = attachEntityToKingdom(kingdomsIdList, personsIdList);
        printEntityToKingdom(entityToKingdom, EntityType.PERSON);
        return entityToKingdom;
    }

    public List<Tuple2> attachDragonToKingdom(List<String> kingdomsIdList, List<String> dragonsIdList) {
        List<Tuple2> entityToKingdom = attachEntityToKingdom(kingdomsIdList, dragonsIdList);
        printEntityToKingdom(entityToKingdom, EntityType.DRAGON);
        return entityToKingdom;
    }

    public List<Tuple2> attachHorseToKingdom(List<String> kingdomsIdList, List<String> horsesIdList) {
        List<Tuple2> entityToKingdom = attachEntityToKingdom(kingdomsIdList, horsesIdList);
        printEntityToKingdom(entityToKingdom, EntityType.HORSE);
        return entityToKingdom;
    }

    public List<Tuple2> attachGuildToKingdom(List<String> kingdomsIdList, List<String> guildsIdList) {
        List<Tuple2> entityToKingdom = attachEntityToKingdom(kingdomsIdList, guildsIdList);
        printEntityToKingdom(entityToKingdom, EntityType.GUILD);
        return entityToKingdom;
    }

    private List<Tuple2> attachAnimalToKingdom(List<String> kingdomsIdList, List<String> animalsIdList) {
        return attachEntityToKingdom(kingdomsIdList, animalsIdList);
    }

    /**
     * @param kingdomsIdList
     * @param entitiesIdList
     * @return Map of assignments <Guild Id, Kingdom Id>
     */
    private List<Tuple2> attachEntityToKingdom(List<String> kingdomsIdList, List<String> entitiesIdList) {
        int totalPopulationSize = entitiesIdList.size();
        List<Tuple2> entitiesToKingdomsSet = new ArrayList<>();
        //We are creating a distribution of kingdoms population size summed up to the kingdoms number
        List<Double> kingdomsPopulationDist = Arrays.stream(RandomUtil.getRandDistArray(kingdomsIdList.size() - 1, 1 - LARGEST_KINGDOM_RATIO))
                .boxed().collect(Collectors.toList());
        // Adding a Kingdom with a large population to skew the kingdom sizes
        kingdomsPopulationDist.add(LARGEST_KINGDOM_RATIO);

        int startEntityId = 0;
        for (int i = 0; i < kingdomsPopulationDist.size(); i++) {
            String kingdomId = kingdomsIdList.get(i);
            int kingdomPopulationSize = Math.toIntExact(Math.round(kingdomsPopulationDist.get(i) * totalPopulationSize));
            for (int entityId = startEntityId; entityId < totalPopulationSize; entityId++) {
                entitiesToKingdomsSet.add(new Tuple2<>(Integer.toString(entityId), kingdomId));
                if (entityId == startEntityId + kingdomPopulationSize)
                    break;
            }
            startEntityId += kingdomPopulationSize + 1;
        }
        return entitiesToKingdomsSet;
    }


    private void printEntityToKingdom(List<Tuple2> entitiesToKingdom, EntityType entityType) {
        List<String[]> e2kRecords = new ArrayList<>();

        for (Tuple2 e2k : entitiesToKingdom) {
            String entityId = e2k._1().toString();
            String kingdomId = e2k._2().toString();
            String edgeId = entityId + "_" + kingdomId;
            Date since = RandomUtil.randomDate(kingdomConf.getStartDateOfStory(), kingdomConf.getEndDateOfStory());

            RelationBase entityToKingdomRel = null;
            if (entityType == EntityType.HORSE || entityType == EntityType.DRAGON) {
                entityToKingdomRel = new Originated(edgeId, entityId, kingdomId, since);
            }
            if (entityType == EntityType.GUILD) {
                entityToKingdomRel = new Registered(edgeId, entityId, kingdomId, since);
            }
            if (entityType == EntityType.PERSON) {
                entityToKingdomRel = new SubjectOf(edgeId, entityId, kingdomId, since);
            }

            assert entityToKingdomRel != null;
            e2kRecords.add(entityToKingdomRel.getRecord());
        }
        //Write graph
        String relationsFile = null;
        if (entityType == EntityType.HORSE || entityType == EntityType.DRAGON) {
            relationsFile = String.format("%s_%s_%s.csv",
                    kingdomConf.getRelationsFilePath().replace(".csv", ""),
                    RelationType.ORIGINATED,
                    entityType);
        }
        if (entityType == EntityType.GUILD) {
            relationsFile = String.format("%s_%s_%s.csv",
                    kingdomConf.getRelationsFilePath().replace(".csv", ""),
                    RelationType.REGISTERED,
                    entityType);
        }
        if (entityType == EntityType.PERSON) {
            relationsFile = String.format("%s_%s_%s.csv",
                    kingdomConf.getRelationsFilePath().replace(".csv", ""),
                    RelationType.SUBJECT_OF,
                    entityType);
        }
        CsvUtil.appendResults(e2kRecords, relationsFile);
    }

    //region Fields
    private final KingdomConfiguration kingdomConf;
    //endregion

}
