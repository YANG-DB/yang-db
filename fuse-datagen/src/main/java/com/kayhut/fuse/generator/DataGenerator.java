package com.kayhut.fuse.generator;

import com.google.common.base.Stopwatch;
import com.kayhut.fuse.generator.configuration.*;
import com.kayhut.fuse.generator.data.generation.entity.GuildGenerator;
import com.kayhut.fuse.generator.data.generation.entity.KingdomGenerator;
import com.kayhut.fuse.generator.data.generation.graph.DragonsGraphGenerator;
import com.kayhut.fuse.generator.data.generation.scale.free.barbasi.albert.graphstream.GraphstreamHelper;
import com.kayhut.fuse.generator.model.entity.Guild;
import com.kayhut.fuse.generator.model.entity.Kingdom;
import com.kayhut.fuse.generator.util.RandomUtil;
import javaslang.Tuple2;
import org.apache.commons.configuration.Configuration;
import org.graphstream.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    //Not all of the population is member of guild
    private static final double NOT_ASSIGNED_TO_GUILD_RATIO = 0.025;
    //The shape parameter in  Exponential distribution
    private static final double LAMBDA_EXP_DIST = 0.5;
    //Used to skew the results
    private static final double LARGEST_KINGDOM_RATIO = 0.3;

    public static void main(String[] args) {

        if (!isValidNumberOfArguments(args)) {
            System.exit(-1);
        }
        loadConfiguration(args[0]);
        //GenerateSmallDragonsGraph(logger, configuration, false);
        //generateMassiveDragonsGraph(logger, configuration);
    }

    public static void loadConfiguration(String path) {
        configuration = new DataGenConfiguration(path).getInstance();
    }

    public static void generateSmallDragonsGraph(Configuration configuration, boolean drawGraph) {
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
            logger.error(e.getMessage(), e);
        }
    }

    public static void generateMassiveDragonsGraph(Configuration configuration) {
        try {
            Stopwatch stopwatch = Stopwatch.createStarted();
            DragonsGraphGenerator massiveDragonsGraphGenerator = new DragonsGraphGenerator(new DragonConfiguration(configuration));
            massiveDragonsGraphGenerator.generateMassiveGraph();
            stopwatch.stop();
            long elapsed = stopwatch.elapsed(TimeUnit.SECONDS);
            logger.info("Dragons massive graph generation took (seconds): %d", elapsed);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static List<Kingdom> generateKingdoms(Configuration configuration) {
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
            logger.error(e.toString(), e);
        }
        return kingdomsList;
    }

    public static List<Guild> generateGuilds(Configuration configuration) {
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
            logger.error(e.toString(), e);
        }
        return guildsList;
    }

    /**
     * @param kingdomsIdList 0...Number of kingdoms
     * @param personsIdList  0...Number of persons
     * @return EdgeSet ( List of Tuple<Person Id, Kingdom ID>) of relationships between Persons and Kingdoms.
     */
    public static List<Tuple2> attachPersonsToKingdoms(List<Integer> kingdomsIdList, List<Integer> personsIdList) {
        int totalPopulationSize = personsIdList.size();
        List<Tuple2> personsToKingdomsSet = new ArrayList<>();
        //We are creating a distribution of kingdoms population size summed up to the kingdoms number
        List<Double> kingdomsPopulationDist = Arrays.stream(RandomUtil.getRandDistArray(kingdomsIdList.size() - 1, 1 - LARGEST_KINGDOM_RATIO))
                .boxed().collect(Collectors.toList());
        // Adding a Kingdom with a large population to skew the kingdom sizes
        kingdomsPopulationDist.add(LARGEST_KINGDOM_RATIO);

        int startPersonId = 0;
        for (int i = 0; i < kingdomsPopulationDist.size(); i++) {
            int kingdomPopulationSize = Math.toIntExact(Math.round(kingdomsPopulationDist.get(i) * totalPopulationSize));
            for (int personId = startPersonId; personId < totalPopulationSize; personId++) {
                personsToKingdomsSet.add(new Tuple2<>(personId, i));
                if (personId == startPersonId + kingdomPopulationSize)
                    break;
            }
            startPersonId += kingdomPopulationSize + 1;
        }
        return personsToKingdomsSet;
    }

    public static Map<Integer, List<Integer>> attachPersonsToGuilds(List<Integer> guildsIdList, List<Integer> personsIdList) {
        Map<Integer, List<Integer>> guildToPersonsSet = new HashMap<>();

        PersonConfiguration personConf = new PersonConfiguration(configuration);
        int maxGuildMembership = personConf.getMaxGuildMembership();
        int membersPopulationSize = Math.toIntExact(Math.round(personsIdList.size() * (1 - NOT_ASSIGNED_TO_GUILD_RATIO)));
        //One person can be belong to several guilds <Guild Id, List of persons Ids>

        //We are creating an Exp distribution of guild size summed up to the guilds number
        double[] expDistArray = RandomUtil.getExpDistArray(guildsIdList.size(), 1.0 - NOT_ASSIGNED_TO_GUILD_RATIO, LAMBDA_EXP_DIST);
        List<Double> guildsMembersDist = Arrays.stream(expDistArray).boxed().collect(Collectors.toList());

        List<Integer> shuffledPersonsIds = IntStream.rangeClosed(0, membersPopulationSize)
                .boxed().collect(Collectors.toList());
        //Adding the same persons to several guilds - without changing the ratio of each guild
        for (int k = 0; k < maxGuildMembership; k++) {
            int startIndex = 0;
            for (int i = 0; i < guildsMembersDist.size(); i++) {
                int guildMembersSize = Math.toIntExact(Math.round(guildsMembersDist.get(i) * membersPopulationSize));
                for (int j = startIndex; j < membersPopulationSize; j++) {

                    Integer personId = shuffledPersonsIds.get(j);

                    if (guildToPersonsSet.get(i) == null) {
                        guildToPersonsSet.put(i, new ArrayList<>(Arrays.asList(personId)));
                    } else {
                        List<Integer> personsInGuild = guildToPersonsSet.get(i);
                        if (!personsInGuild.contains(personId)) { //avoiding duplicate members in a Guild
                            guildToPersonsSet.get(i).add(personId);
                        }
                    }
                    if (j == startIndex + guildMembersSize)
                        break;
                }
                startIndex += guildMembersSize + 1;
            }
            Collections.shuffle(shuffledPersonsIds);
        }
        return guildToPersonsSet;
    }

    public static Map<Integer, List<Integer>> attachDragonsToPersons(List<Integer> dragonsIdList, List<Integer> personsIdList) {
        Map<Integer, List<Integer>> dragonsToPersonsSet = new HashMap<>();

        //Deep Copy
        List<Integer> dragonsIdsClone = new ArrayList<>();
        for(int d : dragonsIdList) {
            dragonsIdsClone.add(d);
        }

        int meanDragonsPerPerson = (int)(dragonsIdList.size() / (double)personsIdList.size());
        //Generate Gaussian of numbers that will represent the ownership of dragons by person
        List<Double> gaussianDist = RandomUtil.randomGaussianNumbers(meanDragonsPerPerson, 2, personsIdList.size());

        //Shuffle the clone list to make it randomize
        Collections.shuffle(dragonsIdsClone);

        int startSelectNum = 0;
        //The gaussian Dist list is the same size of the persons ids
        for (int i = 0; i < gaussianDist.size(); i++) {
            int randNumOfDragonsPerPerson = (int) Math.floor(gaussianDist.get(i));
            int personId = personsIdList.get(i);
            List<Integer> selectedDragonsIds = dragonsIdsClone.subList(startSelectNum, startSelectNum + randNumOfDragonsPerPerson);
            startSelectNum = randNumOfDragonsPerPerson;

            if (dragonsToPersonsSet.get(personId) == null) {
                dragonsToPersonsSet.put(personId, new ArrayList<>(selectedDragonsIds));
            } else {
                List<Integer> personDragons = dragonsToPersonsSet.get(personId);
//                if (!personDragons.containsAll()ins(personId)) { //avoiding duplicate members in a Guild
//                    guildToPersonsSet.get(i).add(personId);
//                }
            }
        }
        return null;
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

    private static String GetExecutionPath() {
        String absolutePath = DataGenerator.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath();
        absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf('/'));
        absolutePath = absolutePath.replaceAll("%20", " ");
        return absolutePath;
    }

    //endregion


}
