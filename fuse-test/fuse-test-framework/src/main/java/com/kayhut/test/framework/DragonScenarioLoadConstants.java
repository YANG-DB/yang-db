package com.kayhut.test.framework;

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
            "CREATE (p:Person { id: toInt(csvLine[0]), name: csvLine[1] , gender:csvLine[2], height: toInt(csvLine[3]), birthDate: csvLine[4], deathDate:csvLine[5] })";

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
            "MATCH (person:Person { id: toInt(csvLine[0])}),(horse:Horse { id: toInt(csvLine[2])})"+
            "CREATE (person)-[:OWNS { since: csvLine[4], till:csvLine[5] }]->(horse)";

    public static String LOAD_PERSON_OWNS_DRAGON_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (person:Person { id: toInt(csvLine[0])}),(dragon:Dragon { id: toInt(csvLine[2])})"+
            "CREATE (person)-[:OWNS { since: csvLine[4], till:csvLine[5] }]->(dragon)";

    public static String LOAD_DRAGON_FIRES_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (d1:Dragon { id: toInt(csvLine[0])}),(d2:Dragon { id: toInt(csvLine[2])})"+
            "CREATE (d1)-[:FIRES_AT { time:csvLine[4]}]->(d2)";

    public static String LOAD_DRAGON_FREEZES_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (d1:Dragon { id: toInt(csvLine[0])}),(d2:Dragon { id: toInt(csvLine[2])})"+
            "CREATE (d1)-[:FREEZES { time:csvLine[4], duration:csvLine[5]}]->(d2)";

    public static String LOAD_PERSON_OFFSPRING_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (p1:Person { id: toInt(csvLine[0])}),(p2:Person { id: toInt(csvLine[2])})"+
            "CREATE (p1)-[:PARENT ]->(p2)";

    public static String LOAD_PERSON_KNOWS_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (p1:Person { id: toInt(csvLine[0])}),(p2:Person { id: toInt(csvLine[2])})"+
            "CREATE (p1)-[:KNOWS {since: csvLine[4]} ]->(p2)";

    public static String LOAD_PERSON_MEMBER_OF_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (person:Person { id: toInt(csvLine[0])}),(guild:Guild { id: toInt(csvLine[2])})"+
            "CREATE (person)-[:MEMBER_OF {since: csvLine[4], till: csvLine[5]} ]->(guild)";

    public static String LOAD_PERSON_SUBJECT_OF_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (person:Person { id: toInt(csvLine[0])}),(kingdom:Kingdom { id: toInt(csvLine[2])})"+
            "CREATE (person)-[:SUBJECT_OF ]->(kingdom)";

    public static String LOAD_GUILD_REGISTERED_IN_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (guild:Guild { id: toInt(csvLine[0])}),(kingdom:Kingdom { id: toInt(csvLine[2])})"+
            "CREATE (guild)-[:REGISTERED_IN ]->(kingdom)";

    public static String LOAD_HORSE_ORIGINATED_IN_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (horse:Horse { id: toInt(csvLine[0])}),(kingdom:Kingdom { id: toInt(csvLine[2])})"+
            "CREATE (horse)-[:ORIGINATED_IN ]->(kingdom)";

    public static String LOAD_DRAGON_ORIGINATED_IN_TEMPLATE = "USING PERIODIC COMMIT 500\n" +
            "LOAD CSV FROM \"%s\" AS csvLine\n" +
            "MATCH (dragon:Dragon { id: toInt(csvLine[0])}),(kingdom:Kingdom { id: toInt(csvLine[2])})"+
            "CREATE (dragon)-[:ORIGINATED_IN ]->(kingdom)";
}
