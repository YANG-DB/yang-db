package com.kayhut.fuse.generator.data.generation;

import com.kayhut.fuse.generator.configuration.KingdomConfiguration;
import com.kayhut.fuse.generator.data.generation.entity.KingdomGenerator;
import com.kayhut.fuse.generator.model.entity.EntityBase;
import com.kayhut.fuse.generator.model.entity.Kingdom;
import com.kayhut.fuse.generator.util.CsvUtil;
import com.kayhut.fuse.generator.util.RandomUtil;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
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
        List<Kingdom> kingdoms = generateKingdoms();
        return Stream.ofAll(kingdoms).map(EntityBase::getId).toJavaList();
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
    public List<Tuple2> attachPersonToKingdoms(List<String> kingdomsIdList, List<String> personsIdList) {

        return attachEntityToKingdom(kingdomsIdList, personsIdList);
    }

    public List<Tuple2> attachDragonToKingdom(List<String> kingdomsIdList, List<String> dragonsIdList) {
        return attachEntityToKingdom(kingdomsIdList, dragonsIdList);
    }

    public List<Tuple2> attachHorseToKingdom(List<String> kingdomsIdList, List<String> horsesIdList) {
        return attachEntityToKingdom(kingdomsIdList, horsesIdList);
    }

    public List<Tuple2> attachGuildToKingdom(List<String> kingdomsIdList, List<String> guildsIdList) {
        return attachEntityToKingdom(kingdomsIdList, guildsIdList);
    }

    private List<Tuple2> attachAnimalToKingdom(List<String> kingdomsIdList, List<String> animalsIdList) {
        return attachEntityToKingdom(kingdomsIdList, animalsIdList);
    }

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
            int kingdomPopulationSize = Math.toIntExact(Math.round(kingdomsPopulationDist.get(i) * totalPopulationSize));
            for (int entityId = startEntityId; entityId < totalPopulationSize; entityId++) {
                entitiesToKingdomsSet.add(new Tuple2<>(entityId, i));
                if (entityId == startEntityId + kingdomPopulationSize)
                    break;
            }
            startEntityId += kingdomPopulationSize + 1;
        }
        return entitiesToKingdomsSet;
    }

    //region Fields
    private final KingdomConfiguration kingdomConf;
    //endregion

}
