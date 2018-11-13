package com.kayhut.fuse.generator;

/*-
 * #%L
 * fuse-domain-gragons-datagen
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

import com.kayhut.fuse.generator.configuration.*;
import com.kayhut.fuse.generator.data.generation.*;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


/**
 * Created by benishue on 15-May-17.
 */
public class DataGenerator {

    private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);
    private final static int MIN_NUM_OF_ARGUMENTS = 1;
    private static Configuration configuration;


    public static void main(String[] args) {
        if (!isValidNumberOfArguments(args)) {
            System.exit(-1);
        }
        loadConfiguration(args[0]);

        try {
            DragonsGraphGenerator dgg = new DragonsGraphGenerator(new DragonConfiguration(configuration));
            List<String> dragonsIds = dgg.generateMassiveDragonsGraph();

            HorsesGraphGenerator hgg = new HorsesGraphGenerator(new HorseConfiguration(configuration));
            List<String> horsesIds = hgg.generateHorsesGraph();

            PersonConfiguration personConf = new PersonConfiguration(configuration);
            PersonsGraphGenerator pgg = new PersonsGraphGenerator(personConf);
            List<String> personsIds = pgg.generatePersonsGraph();
            pgg.attachDragonsToPerson(dragonsIds, personsIds, personConf.getMeanDragonsPerPerson(), personConf.getSdDragonsPerPerson());
            pgg.attachHorsesToPerson(horsesIds, personsIds, personConf.getMeanHorsesPerPerson(), personConf.getSdHorsesPerPerson());


            GuildsGraphGenerator ggg = new GuildsGraphGenerator(configuration);
            List<String> guildsIds = ggg.generateGuildsGraph();
            ggg.attachPersonsToGuilds(guildsIds, personsIds);

            KingdomsGraphGenerator kgg = new KingdomsGraphGenerator(new KingdomConfiguration(configuration));
            List<String> kingdomsIds = kgg.generateKingdomsGraph();
            kgg.attachDragonToKingdom(kingdomsIds, dragonsIds);
            kgg.attachHorseToKingdom(kingdomsIds, horsesIds);
            kgg.attachGuildToKingdom(kingdomsIds, guildsIds);
            kgg.attachPersonToKingdom(kingdomsIds, personsIds);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void loadConfiguration(String path) {
        try {
            configuration = new DataGenConfiguration(path).getInstance();
            String resultsPath = System.getProperty("user.dir") + File.separator +
                    configuration.getString("resultsPath");
            logger.info("Creating Results Folder: {}", resultsPath);
            Files.createDirectories(Paths.get(resultsPath));
        } catch (Exception e) {
            logger.error("Failed to load configuration", e);
        }

    }


    //region Private Methods
    private static boolean isValidNumberOfArguments(String[] args) {
        if (args.length < MIN_NUM_OF_ARGUMENTS) {
            logger.error("Expected {} argument(s): <path to field configuration file>", MIN_NUM_OF_ARGUMENTS);
            return false;
        }
        return true;
    }
    //endregion

}
