package com.kayhut.fuse.generator;
import com.google.common.base.Stopwatch;
import com.kayhut.fuse.generator.generator.dragon.DragonConfiguration;
import com.kayhut.fuse.generator.generator.dragon.DragonsGraphGeneratorV1;
import com.kayhut.fuse.generator.generator.dragon.DragonsGraphGeneratorV3;
import com.kayhut.fuse.generator.generator.graph.barbasi.albert.graphstream.GraphstreamHelper;
import com.kayhut.fuse.generator.configuration.DataGenConfiguration;
import org.apache.commons.configuration.Configuration;
import org.graphstream.graph.Graph;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * Created by benishue on 15-May-17.
 */
public class DataGenerator {

    public static void main(String[] args) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(DataGenerator.class);
        validateNumberOfArguments(args, logger);
        Configuration configuration = new DataGenConfiguration(args[0]).getInstance();
        //GenerateSmallDragonsGraph(logger, configuration, false);
        generateMassiveDragonsGraph(logger, configuration);
    }

    private static void generateSmallDragonsGraph(Logger logger,
                                                  Configuration configuration,
                                                  boolean drawGraph)  {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            DragonsGraphGeneratorV1 dragonsGraphGeneratorV1 = new DragonsGraphGeneratorV1(new DragonConfiguration(configuration));
            Graph dragonsInteractionGraph = dragonsGraphGeneratorV1.generateDragonsGraph();
            stopwatch.stop();
            long elapsed = stopwatch.elapsed(TimeUnit.SECONDS);
            logger.info("Dragons graph V1 generation took (seconds): %d", elapsed);

            GraphstreamHelper.printScaleFreeDataSummary(dragonsInteractionGraph, configuration.getString("resultsPath"));
            if (drawGraph) {
                GraphstreamHelper.drawGraph(dragonsInteractionGraph, logger);
                System.in.read();
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private static void generateMassiveDragonsGraph(Logger logger, Configuration configuration) {

        Stopwatch stopwatch = Stopwatch.createStarted();
        DragonsGraphGeneratorV3 massiveDragonsGraphGenerator = new DragonsGraphGeneratorV3(new DragonConfiguration(configuration));
        massiveDragonsGraphGenerator.generateDragonsGraph();
        stopwatch.stop();
        long elapsed = stopwatch.elapsed(TimeUnit.SECONDS);
        logger.info("Dragons graph V3 generation took (seconds): %d", elapsed);

    }


    //region Private Methods
    private static void validateNumberOfArguments(String[] args, Logger logger) {
        if (args.length < MIN_NUM_OF_ARGUMENTS) {
            logger.error("Expected %d argument(s): ", MIN_NUM_OF_ARGUMENTS);
            logger.error("\n\t<path to field configuration file>");
            System.exit(-1);
        }
    }
    private static String GetExecutionPath() {
        String absolutePath = DataGenerator.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath();
        absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf('/'));
        absolutePath = absolutePath.replaceAll("%20", " ");
        return absolutePath;
    }

    //endregion

    //region static fields
    private final static int MIN_NUM_OF_ARGUMENTS = 1;
    //endregion
}
