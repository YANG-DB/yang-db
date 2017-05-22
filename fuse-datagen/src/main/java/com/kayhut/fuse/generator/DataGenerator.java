package com.kayhut.fuse.generator;

import com.google.common.base.Stopwatch;
import com.kayhut.fuse.generator.configuration.DragonConfiguration;
import com.kayhut.fuse.generator.configuration.GuildConfiguration;
import com.kayhut.fuse.generator.configuration.KingdomConfiguration;
import com.kayhut.fuse.generator.data.generation.entity.GuildGenerator;
import com.kayhut.fuse.generator.data.generation.entity.KingdomGenerator;
import com.kayhut.fuse.generator.data.generation.graph.DragonsGraphGenerator;
import com.kayhut.fuse.generator.configuration.DataGenConfiguration;
import com.kayhut.fuse.generator.data.generation.model.barbasi.albert.graphstream.GraphstreamHelper;
import com.kayhut.fuse.generator.model.entity.Guild;
import com.kayhut.fuse.generator.model.entity.Kingdom;
import com.kayhut.fuse.generator.util.RandomUtil;
import javaslang.Tuple2;
import org.apache.commons.configuration.Configuration;
import org.graphstream.graph.Graph;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Created by benishue on 15-May-17.
 */
public class DataGenerator {

    public static void main(String[] args) {
        Logger logger = org.slf4j.LoggerFactory.getLogger(DataGenerator.class);
        if (!isValidNumberOfArguments(args, logger))
            System.exit(-1);
        Configuration configuration = new DataGenConfiguration(args[0]).getInstance();
        //GenerateSmallDragonsGraph(logger, configuration, false);
        //generateMassiveDragonsGraph(logger, configuration);
    }

    public static void generateSmallDragonsGraph(Logger logger,
                                                 Configuration configuration,
                                                 boolean drawGraph) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            DragonsGraphGenerator dragonsGraphGenerator = new DragonsGraphGenerator(new DragonConfiguration(configuration));
            Graph dragonsInteractionGraph = dragonsGraphGenerator.generateGraph();
            stopwatch.stop();
            long elapsed = stopwatch.elapsed(TimeUnit.SECONDS);
            logger.info("Dragons model V1 generation took (seconds): %d", elapsed);

            GraphstreamHelper.printScaleFreeDataSummary(dragonsInteractionGraph, configuration.getString("resultsPath"));
            if (drawGraph) {
                GraphstreamHelper.drawGraph(dragonsInteractionGraph, logger);
                System.in.read();
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public static void generateMassiveDragonsGraph(Logger logger, Configuration configuration) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            DragonsGraphGenerator massiveDragonsGraphGenerator = new DragonsGraphGenerator(new DragonConfiguration(configuration));
            massiveDragonsGraphGenerator.generateMassiveGraph();
            stopwatch.stop();
            long elapsed = stopwatch.elapsed(TimeUnit.SECONDS);
            logger.info("Dragons massive graph generation took (seconds): %d", elapsed);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public static List<Kingdom> generateKingdoms(Logger logger, Configuration configuration) {
        List<Kingdom> kingdomsList = new ArrayList<>();
        try {
            KingdomConfiguration kingdomConfiguration = new KingdomConfiguration(configuration);
            KingdomGenerator generator = new KingdomGenerator(kingdomConfiguration);

            //In cases we want to generate only part of the kingdoms
            int kingdomsSize = kingdomConfiguration.getNumberOfNodes();
            String[] kingdomsNames = kingdomConfiguration.getKingdoms();

            for (int i = 0; i < kingdomsSize; i++) {
                Kingdom kingdom = generator.generate();
                kingdom.setName(kingdomsNames[i]);
                kingdom.setId(Integer.toString(i));
                kingdomsList.add(kingdom);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return kingdomsList;
    }


    public static List<Guild> generateGuilds(Logger logger, Configuration configuration) {
        List<Guild> guildsList = new ArrayList<>();

        try {
            GuildConfiguration guildConfiguration = new GuildConfiguration(configuration);
            GuildGenerator generator = new GuildGenerator(guildConfiguration);

            //In cases we want to generate only part of the guilds
            int guildsSize = guildConfiguration.getNumberOfNodes();
            String[] guildsNames = guildConfiguration.getGuilds();

            for (int i = 0; i < guildsSize; i++) {
                Guild guild = generator.generate();
                guild.setName(guildsNames[i]);
                guild.setId(Integer.toString(i));
                guildsList.add(guild);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return guildsList;
    }

    /**
     * @param kingdomsIdList 0...Number of kingdoms
     * @param personsIdList 0...Number of persons
     * @return EdgeSet ( List of Tuple<Person Id, Kingdom ID>) of relationships between Persons and Kingdoms.
     *
     */
    public static List<Tuple2> attachPersonsToKingdoms(List<Integer> kingdomsIdList, List<Integer> personsIdList) {
        final double largestKingdomRatio = 0.3; //Used to skew the distribution
        int totalPopulationSize = personsIdList.size();
        List<Tuple2> personsToKingdomsSet = new ArrayList<>();
        //We are creating a distribution of kingdoms population size summed up to the kingdoms number
        List<Double> kingdomsPopulationDist = Arrays.stream(RandomUtil.getRandDistArray(kingdomsIdList.size() - 1, 1 - largestKingdomRatio)).boxed().collect(Collectors.toList());
        // Adding a Kingdom with a large population to skew the kingdom sizes
        kingdomsPopulationDist.add(largestKingdomRatio);

        int startPerosnId = 0;
        for (int i = 0; i < kingdomsPopulationDist.size(); i++) {
            int kingdomPopulationSize = Math.toIntExact(Math.round(kingdomsPopulationDist.get(i) * totalPopulationSize));
            for (int personId = startPerosnId; personId < totalPopulationSize; personId++) {
                personsToKingdomsSet.add(new Tuple2<>(personId, i));
                if (personId == startPerosnId + kingdomPopulationSize)
                    break;
            }
            startPerosnId += kingdomPopulationSize + 1;
        }
        return personsToKingdomsSet;
    }

    public static List<Tuple2> attachPersonsToGuilds(List<Integer> guildsIdList, List<Integer> personsIdList) {
        final double notAssignedRatio = 0.025; //precentage of persons not assigned to any guild
        int totalPopulationSize = personsIdList.size();
        List<Tuple2> personsToGuildsSet = new ArrayList<>();
        //We are creating an exp distribution of guild size summed up to the kingdoms number
        double[] expDistArray = RandomUtil.getExpDistArray(guildsIdList.size() - 1, 1 - notAssignedRatio, 0.5);
        List<Double> guildsMembersDist = Arrays.stream(expDistArray).boxed().collect(Collectors.toList());
        double[] cumulativeDistArray = RandomUtil.getCumulativeDistArray(expDistArray);

        // Adding a 'artficial - not member' guild - the persons belong to this
        guildsMembersDist.add(notAssignedRatio);

        int startPerosnId = 0;
        for (int i = 0; i < guildsMembersDist.size(); i++) {
            int guildMembersSize = Math.toIntExact(Math.round(guildsMembersDist.get(i) * totalPopulationSize));
            for (int personId = startPerosnId; personId < totalPopulationSize * (1-notAssignedRatio); personId++) {
                //The last persons do not belong to any guild
                personsToGuildsSet.add(new Tuple2<>(personId, i));
                if (personId == startPerosnId + guildMembersSize)
                    break;
            }
            startPerosnId += guildMembersSize + 1;
        }

        //Adding the same persons to severals guilds - without changing the ratio of each guild
        //RandomUtil.randomInt()

        return personsToGuildsSet;
    }


    //region Private Methods
    private static boolean isValidNumberOfArguments(String[] args, Logger logger) {
        if (args.length < MIN_NUM_OF_ARGUMENTS) {
            logger.error("Expected %d argument(s): ", MIN_NUM_OF_ARGUMENTS);
            logger.error("\n\t<path to field configuration file>");
            return false;
        }
        return true;
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
