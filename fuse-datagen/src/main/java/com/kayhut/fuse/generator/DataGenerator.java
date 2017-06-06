package com.kayhut.fuse.generator;

import com.google.common.base.Stopwatch;
import com.kayhut.fuse.generator.configuration.*;
import com.kayhut.fuse.generator.data.generation.*;
import com.kayhut.fuse.generator.data.generation.entity.GuildGenerator;
import com.kayhut.fuse.generator.data.generation.entity.KingdomGenerator;
import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.graphstream.GraphstreamHelper;
import com.kayhut.fuse.generator.model.entity.EntityBase;
import com.kayhut.fuse.generator.model.entity.Guild;
import com.kayhut.fuse.generator.model.entity.Kingdom;
import com.kayhut.fuse.generator.util.CsvUtil;
import com.kayhut.fuse.generator.util.RandomUtil;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.apache.commons.configuration.Configuration;
import org.graphstream.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * Created by benishue on 15-May-17.
 */
public class DataGenerator {

    private static final Logger logger = LoggerFactory.getLogger(DataGenerator.class);
    private final static int MIN_NUM_OF_ARGUMENTS = 1;
    private static Configuration configuration;


    //The shape parameter in  Exponential distribution
    private static final double LAMBDA_EXP_DIST = 0.5;


    public static void main(String[] args) {
        if (!isValidNumberOfArguments(args)) {
            System.exit(-1);
        }
        loadConfiguration(args[0]);

        DragonsGraphGenerator dgg = new DragonsGraphGenerator(new DragonConfiguration(configuration));
        List<String> dragonsIds = dgg.generateMassiveDragonsGraph();

        PersonsGraphGenerator pgg = new PersonsGraphGenerator(new PersonConfiguration(configuration));
        List<String> personsIds = pgg.generatePersonsGraph();

        HorsesGraphGenerator hgg = new HorsesGraphGenerator(new HorseConfiguration(configuration));
        List<String> horsesIds = hgg.generateHorsesGraph();

        KingdomsGraphGenerator kgg = new KingdomsGraphGenerator(new KingdomConfiguration(configuration));
        List<String> kingdomsIds = kgg.generateKingdomsGraph();

        GuildsGraphGenerator ggg = new GuildsGraphGenerator(configuration);
        List<String> guildsIds = ggg.generateGuildsGraph();
    }

    public static void loadConfiguration(String path) {
        try {
            configuration = new DataGenConfiguration(path).getInstance();
            String resultsPath = System.getProperty("user.dir") + File.separator +
                    configuration.getString("resultsPath");
            logger.info("Creating Results Folder: " + resultsPath);
            Files.createDirectories(Paths.get(resultsPath));
        } catch (Exception e) {
            logger.error("Failed to load configuration", e);
        }

    }


    //region Private Methods
    private static boolean isValidNumberOfArguments(String[] args) {
        if (args.length < MIN_NUM_OF_ARGUMENTS) {
            logger.error("Expected %d argument(s): ", MIN_NUM_OF_ARGUMENTS);
            logger.error("\n\t<path to field configuration file>");
            return false;
        }
        return true;
    }
    //endregion

}
