package com.kayhut.test;

/**
 * Created by moti on 3/21/2017.
 */
public class DragonScenarioLoadConstants {
    private static String LOAD_CSV_NO_MATCH_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "CREATE %s";

    private static String LOAD_CSV_MATCH_TEMPLATE= "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH %s\n"+
            "CREATE %s";

    public static String LOAD_PERSONS_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "CREATE (p:Person { id: toInt(csvLine[0]), firstName: csvLine[1] ,lastName:csvLine[2],  gender:csvLine[3], height: toInt(csvLine[4]), birthDate: toInt(csvLine[5]), deathDate:toInt(csvLine[6]) })";

    public static String LOAD_HORSES_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "CREATE (p:Horse { id: toInt(csvLine[0]), name: csvLine[1] , color:csvLine[2], weight: toInt(csvLine[3])})";

    public static String LOAD_DRAGONS_TEMPLATE ="USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "CREATE (p:Dragon { id: toInt(csvLine[0]), name: csvLine[1]})";

    public static String LOAD_GUILDS_TEMPLATE ="USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "CREATE (p:Guild { id: toInt(csvLine[0]), name: csvLine[1]})";

    public static String LOAD_KINGDOMS_TEMPLATE ="USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "CREATE (p:Kingdom { id: toInt(csvLine[0]), name: csvLine[1]})";

    public static String LOAD_PERSON_OWNS_HORSE_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (person:Person { id: toInt(csvLine[0])}),(horse:Horse { id: toInt(csvLine[1])})"+
            "CREATE (person)-[:OWNS { since: toInt(csvLine[2]), till:toInt(csvLine[3]) }]->(horse)";

    public static String LOAD_PERSON_OWNS_DRAGON_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (person:Person { id: toInt(csvLine[0])}),(dragon:Dragon { id: toInt(csvLine[1])})"+
            "CREATE (person)-[:OWNS { since: toInt(csvLine[2]), till:toInt(csvLine[3]) }]->(dragon)";

    public static String LOAD_DRAGON_FIRES_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (d1:Dragon { id: toInt(csvLine[0])}),(d2:Dragon { id: toInt(csvLine[1])})"+
            "CREATE (d1)-[:FIRES_AT { time:toInt(csvLine[2])}]->(d2)";

    public static String LOAD_DRAGON_FREEZES_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (d1:Dragon { id: toInt(csvLine[0])}),(d2:Dragon { id: toInt(csvLine[1])})"+
            "CREATE (d1)-[:FREEZES { time:toInt(csvLine[2]), duration:toInt(csvLine[3])}]->(d2)";

    public static String LOAD_PERSON_OFFSPRING_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (p1:Person { id: toInt(csvLine[0])}),(p2:Person { id: toInt(csvLine[1])})"+
            "CREATE (p1)-[:PARENT ]->(p2)";

    public static String LOAD_PERSON_KNOWS_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (p1:Person { id: toInt(csvLine[0])}),(p2:Person { id: toInt(csvLine[1])})"+
            "CREATE (p1)-[:KNOWS {since: toInt(csvLine[2])} ]->(p2)";

    public static String LOAD_PERSON_MEMBER_OF_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (person:Person { id: toInt(csvLine[0])}),(guild:Guild { id: toInt(csvLine[1])})"+
            "CREATE (person)-[:MEMBER_OF {since: toInt(csvLine[2]), till: toInt(csvLine[3])} ]->(guild)";

    public static String LOAD_PERSON_SUBJECT_OF_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (person:Person { id: toInt(csvLine[0])}),(kingdom:Kingdom { id: toInt(csvLine[1])})"+
            "CREATE (person)-[:SUBJECT_OF ]->(kingdom)";

    public static String LOAD_GUILD_REGISTERED_IN_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (guild:Guild { id: toInt(csvLine[0])}),(kingdom:Kingdom { id: toInt(csvLine[1])})"+
            "CREATE (guild)-[:REGISTERED_IN ]->(kingdom)";

    public static String LOAD_HORSE_ORIGINATED_IN_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (horse:Horse { id: toInt(csvLine[0])}),(kingdom:Kingdom { id: toInt(csvLine[1])})"+
            "CREATE (horse)-[:ORIGINATED_IN ]->(kingdom)";

    public static String LOAD_DRAGON_ORIGINATED_IN_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (dragon:Dragon { id: toInt(csvLine[0])}),(kingdom:Kingdom { id: toInt(csvLine[1])})"+
            "CREATE (dragon)-[:ORIGINATED_IN ]->(kingdom)";
}
