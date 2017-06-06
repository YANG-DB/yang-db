package com.kayhut.fuse.generator.data.generation;

import com.kayhut.fuse.generator.configuration.HorseConfiguration;
import com.kayhut.fuse.generator.data.generation.entity.HorseGenerator;
import com.kayhut.fuse.generator.model.entity.EntityBase;
import com.kayhut.fuse.generator.model.entity.Horse;
import com.kayhut.fuse.generator.util.CsvUtil;
import javaslang.collection.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 05/06/2017.
 */
public class HorsesGraphGenerator {

    private final Logger logger = LoggerFactory.getLogger(HorsesGraphGenerator.class);

    public HorsesGraphGenerator(final HorseConfiguration configuration) {
        this.horseConf =configuration;
    }

    public List<String> generateHorsesGraph() {
        List<Horse> horses = generateHorses();
        return Stream.ofAll(horses).map(EntityBase::getId).toJavaList();
    }

    public List<Horse> generateHorses() {
        List<Horse> guildsList = new ArrayList<>();
        List<String[]> horsesRecords = new ArrayList<>();
        try {
            HorseGenerator generator = new HorseGenerator(horseConf);
            int guildsSize = horseConf.getNumberOfNodes();

            for (int i = 0; i < guildsSize; i++) {
                Horse horse = generator.generate();
                horse.setId(Integer.toString(i));
                guildsList.add(horse);
                horsesRecords.add(horse.getRecord());
            }
            //Write graph
            CsvUtil.appendResults(horsesRecords, horseConf.getEntitiesFilePath());

        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
        return guildsList;
    }

    //region Fields
    private final HorseConfiguration horseConf;
    //endregion

}
