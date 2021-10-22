package com.yangdb.fuse.generator;

/*-
 * #%L
 * fuse-domain-dragons-datagen
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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



import com.yangdb.fuse.generator.configuration.*;
import com.yangdb.fuse.generator.data.generation.*;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
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
        System.out.println("\n" +
                "                                                            \n"+
                " **********************************************************  \n"+
                " **********************************************************  \n"+
                " _____                                                      \n" +
                "(____ \\                                                     \n" +
                " _   \\ \\ ____ ____  ____  ___  ____   ___                   \n" +
                "| |   | / ___) _  |/ _  |/ _ \\|  _ \\ /___)                  \n" +
                "| |__/ / |  ( ( | ( ( | | |_| | | | |___ |                  \n" +
                "|_____/|_|   \\_||_|\\_|| |\\___/|_| |_(___/                   \n" +
                "                  (_____|                                   \n" +
                "     _____                                                  \n" +
                "    (____ \\       _                                         \n" +
                "     _   \\ \\ ____| |_  ____                                 \n" +
                "    | |   | / _  |  _)/ _  |                                \n" +
                "    | |__/ ( ( | | |_( ( | |                                \n" +
                "    |_____/ \\_||_|\\___)_||_|                                \n" +
                "                                                            \n" +
                "        ______                                              \n" +
                "       / _____)                             _               \n" +
                "      | /  ___  ____ ____   ____  ____ ____| |_  ___   ____ \n" +
                "      | | (___)/ _  )  _ \\ / _  )/ ___) _  |  _)/ _ \\ / ___)\n" +
                "      | \\____/( (/ /| | | ( (/ /| |  ( ( | | |_| |_| | |    \n" +
                "       \\_____/ \\____)_| |_|\\____)_|   \\_||_|\\___)___/|_|    \n" +
                "                                                            \n"+
                " **********************************************************  \n"+
                " **********************************************************  \n"+
                "                                                            \n");
        if (!isValidNumberOfArguments(args)) {
            System.out.println("Not found configuration argument - using default one : 'test.generator.properties' ");
            args = new String[] {Thread.currentThread().getContextClassLoader().getResource("test.generator.properties").getFile()};
        }
        Path path = loadConfiguration(args[0]);

        try {
            System.out.println("************GENERATING DRAGONS GRAPH *************************************************");
            DragonsGraphGenerator dgg = new DragonsGraphGenerator(new DragonConfiguration(configuration));
            List<String> dragonsIds = dgg.generateMassiveDragonsGraph();

            System.out.println("************GENERATING HORSES GRAPH *************************************************");
            HorsesGraphGenerator hgg = new HorsesGraphGenerator(new HorseConfiguration(configuration));
            List<String> horsesIds = hgg.generateHorsesGraph();

            System.out.println("************GENERATING PERSON GRAPH *************************************************");
            PersonConfiguration personConf = new PersonConfiguration(configuration);
            PersonsGraphGenerator pgg = new PersonsGraphGenerator(personConf);
            List<String> personsIds = pgg.generatePersonsGraph();
            pgg.attachDragonsToPerson(dragonsIds, personsIds, personConf.getMeanDragonsPerPerson(), personConf.getSdDragonsPerPerson());
            pgg.attachHorsesToPerson(horsesIds, personsIds, personConf.getMeanHorsesPerPerson(), personConf.getSdHorsesPerPerson());


            System.out.println("************GENERATING GUILDS GRAPH *************************************************");
            GuildsGraphGenerator ggg = new GuildsGraphGenerator(configuration);
            List<String> guildsIds = ggg.generateGuildsGraph();
            ggg.attachPersonsToGuilds(guildsIds, personsIds);

            System.out.println("************GENERATING KINGDOM GRAPH *************************************************");
            KingdomsGraphGenerator kgg = new KingdomsGraphGenerator(new KingdomConfiguration(configuration));
            List<String> kingdomsIds = kgg.generateKingdomsGraph();
            kgg.attachDragonToKingdom(kingdomsIds, dragonsIds);
            kgg.attachHorseToKingdom(kingdomsIds, horsesIds);
            kgg.attachGuildToKingdom(kingdomsIds, guildsIds);
            kgg.attachPersonToKingdom(kingdomsIds, personsIds);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        System.out.println(" **********************************************************  \n");
        System.out.println(String.format("Completed writing graph data to destination folder %s ",path.toString()));
        System.out.println(" **********************************************************  \n");

    }

    public static Path loadConfiguration(String path) {
        try {
            configuration = new DataGenConfiguration(path).getInstance();
            String resultsPath = System.getProperty("user.dir") + File.separator +
                    configuration.getString("resultsPath")!=null ? configuration.getString("resultsPath") : "graphBench";
            logger.info("Creating Results Folder: {}", resultsPath);
            System.out.printf("Creating Results Folder: %s%n", resultsPath);
            return Files.createDirectories(Paths.get(resultsPath));
        } catch (Exception e) {
            logger.error("Failed to load configuration", e);
        }

        return null;
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
